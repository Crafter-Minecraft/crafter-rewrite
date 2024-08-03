package com.crafter.discord.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction
import java.util.*

// TODO: Implement convenient subgroup/command creation
abstract class SlashCommand(
    final override val name: String,
    final override val description: String,
    final override val options: List<OptionData> = emptyList()
) : Command, IExecutableCommand<SlashCommandInteractionEvent> {
    val commandData = Commands.slash(name, description)

    private val localization = ResourceBundleLocalizationFunction
        .fromBundle(ResourceBundle.getBundle("translate/slash_commands"), DiscordLocale.RUSSIAN)
        .build()

    fun addCommandGroup(vararg groupList: SubcommandGroupData): SlashCommand {
        commandData.addSubcommandGroups(groupList.toList())
        return this
    }

    fun addSubCommand(vararg subCommands: SubcommandData): SlashCommand {
        commandData.addSubcommands(subCommands.toList())
        return this
    }

    init {
        commandData
            .addOptions(options)
            .setLocalizationFunction(localization)
            .setGuildOnly(true)
    }
}