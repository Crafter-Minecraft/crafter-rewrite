package com.crafter.implementation.commands

import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.text
import com.crafter.implementation.Navigation
import com.crafter.modrinth
import com.crafter.structure.utilities.embedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object ModCommand : SlashCommand("mod", "Sniff about mods") {
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandGroup) {
            "modrinth" -> modrinthGroup(event)
            "curseforge" -> curseforgeGroup(event)
        }
    }

    private suspend fun curseforgeGroup(event: SlashCommandInteractionEvent) {}

    private suspend fun modrinthGroup(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "search" -> {
                event.deferReply(false).queue()

                val name = event.getOption("name")!!.asString
                val type = event.getOption("type")!!.asString

                val result = modrinth.project.search(name)
                val hitResults = result.hits.filter { it.projectType == type }

                val embedList = runCatching {
                    hitResults.map {
                        embedBuilder {
                            setTitle(it.slug)
                            setUrl(it.url)

                            setDescription("-# License: ${it.license}")
                            appendDescription("\n\n" + it.description)

                            addField(text("mod.mod.needed_on_client_side", event.userLocale), it.clientSide, false)
                            addField(text("mod.needed_on_server_side", event.userLocale), it.serverSide, false)

                            val extraInformation = modrinth.project.get(it.slug)
                            addField(
                                text("mod.mod.game_versions", event.userLocale),
                                extraInformation.gameVersions?.joinToString(", ") ?:
                                    text("mod.mod.unknown_versions", event.userLocale),
                                true
                            )
                            setThumbnail(extraInformation.iconUrl)

                            val urls = mapOf(
                                "discord" to extraInformation.discordUrl,
                                "wiki" to extraInformation.wikiUrl,
                                "issues" to extraInformation.issuesUrl,
                                "source" to extraInformation.sourceUrl
                            )

                            setColor(it.color)
                        }
                    }
                }.onFailure {
                    event.replyWithMessage("mod.failed_request", event.userLocale)
                    return
                }

                val navigation = Navigation(event.channelIdLong, event.user.idLong, embedList.getOrThrow(), event.userLocale)
                navigation.register(event.jda)

                event.hook.apply {
                    editOriginalEmbeds(navigation.builtEmbeds.first()).queue()
                    editOriginalComponents(ActionRow.of(navigation.row)).queue()
                }
            }

            else -> "aboba"
        }
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    init {
        val groups = listOf("modrinth", "curseforge").map {
            SubcommandGroupData(it, "Information about mod/plugin")
                .addSubcommands(
                    SubcommandData("search", "Search mod/plugin")
                        .addOptions(
                            OptionData(OptionType.STRING, "name", "name"),
                            OptionData(OptionType.STRING, "type", "name")
                                .addChoices(ProjectType.entries.map { projectType ->
                                    Choice(
                                        projectType.name.lowercase(),
                                        projectType.name.lowercase()
                                    )
                                })
                        )
                )
        }

        commandData.addSubcommandGroups(groups)
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        super.onStringSelectInteraction(event)
    }

    enum class ProjectType {
        Mod,
        Plugin
    }
}