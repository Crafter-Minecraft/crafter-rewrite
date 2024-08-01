package com.crafter.discord.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction
import java.util.*

class SlashCommand(
    override val name: String,
    override val description: String,
    override val options: List<OptionData>,
    private val block: suspend SlashCommand.(SlashCommandInteractionEvent) -> Unit
) : Command<SlashCommandInteractionEvent> {
    private val jdaInstance = Commands.slash(name, description)

    private val localization = ResourceBundleLocalizationFunction
        .fromBundle(ResourceBundle.getBundle("translate/slash_commands"), DiscordLocale.RUSSIAN)
        .build()

    override suspend fun callback(event: SlashCommandInteractionEvent) = block.invoke(this, event)

    fun get(): SlashCommandData = jdaInstance

    fun addCommandGroup(vararg groupList: SubcommandGroupData): SlashCommand {
        jdaInstance.addSubcommandGroups(groupList.toList())
        return this
    }

    fun addSubCommand(vararg subCommands: SubcommandData): SlashCommand {
        jdaInstance.addSubcommands(subCommands.toList())
        return this
    }

    init {
        jdaInstance
            .addOptions(options)
            .setLocalizationFunction(localization)
            .setGuildOnly(true)
    }
}

fun slash(
    name: String,
    description: String,
    options: List<OptionData> = emptyList(),
    block: suspend SlashCommand.(SlashCommandInteractionEvent) -> Unit
) = SlashCommand(name, description, options, block)
