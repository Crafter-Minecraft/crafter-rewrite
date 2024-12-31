package com.crafter.implementation.commands

import com.crafter.discord.t9n.text
import com.crafter.discord.core.child.SlashCommand
import com.crafter.discord.core.child.Subcommand
import com.crafter.discord.core.child.SubcommandGroup
import com.crafter.implementation.Navigation
import com.crafter.modrinth
import com.crafter.structure.utilities.embedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow

object ModCommand : SlashCommand("mod", "Sniff about mods", {
    subGroup(ModrinthGroup::class)
}) {
    override suspend fun invoke(event: SlashCommandInteractionEvent) {}

    override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    private suspend fun projectEmbed(
        title: String,
        description: String,
        license: String,
        url: String,
        clientSide: String,
        serverSide: String,
        color: Int,
        locale: DiscordLocale
    ) = embedBuilder {
        setTitle(title)
        setUrl(url)

        setDescription("-# License: $license")
        appendDescription("\n\n" + description)

        addField(text("mod.mod.needed_on_client_side", locale), clientSide, false)
        addField(text("mod.needed_on_server_side", locale), serverSide, false)

        val extraInformation = modrinth.project.get(title)
        addField(
            text("mod.mod.game_versions", locale),
            extraInformation.gameVersions?.joinToString(", ") ?:
            text("mod.mod.unknown_versions", locale),
            true
        )
        setThumbnail(extraInformation.iconUrl)

        setColor(color)
    }

    object ModrinthGroup : SubcommandGroup(
        "modrinth",
        "Search across modrinth mods/plugins",
        ModInfoCommand,
        SearchModCommand
    )

    object SearchModCommand : Subcommand("search", "Search mod/plugin", {
        argument<String>("name", "Project name", true)
        argument<String>("type", "Is it plugin or mod?", true, choices = ProjectType.entries.associate {
            it.name.lowercase() to it.name.lowercase()
        })
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.deferReply().queue()
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

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    object ModInfoCommand : Subcommand("info", "Information about mod/plugin", {
        argument<String>("name", "Project name", true)
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.deferReply().queue()
            val name = event.getOption("name")!!.asString

            runCatching {
                modrinth.project.get(name)
            }.onSuccess { project ->
                val embed = projectEmbed(
                    project.slug,
                    project.description,
                    project.license.id,
                    project.url,
                    project.clientSide,
                    project.serverSide,
                    project.color ?: 0,
                    event.userLocale,
                )

                event.hook.sendMessageEmbeds(embed.build()).queue()
            }.onFailure {
                event.hook.sendMessage(text("mod.404", event.userLocale)).queue()
            }
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    enum class ProjectType {
        Mod,
        Plugin
    }
}