package com.crafter.discord.registry

import com.crafter.discord.Initializable
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.discord.core.ConceptCommand
import com.crafter.discord.core.child.SlashCommand
import com.crafter.getDefaultScope
import com.crafter.implementation.commands.*
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.data.SerializableData

class CommandRegistry(private val jda: JDA) : ListenerAdapter(), Initializable {
    private val defaultScope = getDefaultScope()
    private val slashCommandList = listOf(
        PingCommand,
        RconCommand,
        BridgeCommand,
        GuidelineCommand,
        ModCommand
    )

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        defaultScope.launch {
            val command = slashCommandList.firstOrNull { command -> command.name == event.name } ?: return@launch
            val invokableCommand = getCommand(command, event.fullCommandName)

            invokableCommand.invoke(event)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        defaultScope.launch {
            val command = slashCommandList.firstOrNull { it.name == event.name } ?: return@launch
            val invokableCommand = getCommand(command, event.fullCommandName)

            val autoCompleteOptions = invokableCommand.autocomplete(event)
            autoCompleteOptions?.forEach {
                if (event.name == command.name && it.first == event.focusedOption.name) {
                    val options = it.second.filter { word -> word.startsWith(event.focusedOption.value) }

                    event.replyChoiceStrings(options.subList(0, if (options.size > 25) 25 else options.size)).queue()
                }
            }
        }
    }

    private fun getCommand(parent: SlashCommand, fullCommandName: String): ConceptCommand<out SerializableData> {
        val fullCommand = fullCommandName.split(" ")
        if (fullCommand.size == 1) return parent

        if (parent.subgroups.isEmpty()) {
            val subcommand = parent.subcommands.firstOrNull { subCommand -> subCommand.name == fullCommand[1] }
                ?: return parent

            return subcommand
        }

        parent.subgroups.forEach { subGroup ->
            val subcommand = subGroup.subcommands.firstOrNull { subCommand -> subCommand.name == fullCommand[2] }
                ?: return parent

            return subcommand
        }

        return parent
    }

    override fun initialize() {
        jda.addEventListener(this)

        slashCommandList.forEach { command ->
            localizeCommand(command.commandData)
            command.build()

            jda.addEventListener(command)
        }

        jda.updateCommands()
            .addCommands(slashCommandList.map { it.commandData })
            .queue()
    }

    private fun localizeCommand(data: SlashCommandData) {
        data.setLocalizationFunction { key ->
            val localizations = HashMap<DiscordLocale, String>()

            T9nProtocol.AVAILABLE_LOCALES.forEach { locale ->
                val discordLocale = DiscordLocale.from(locale)

                // Skipping because it's always english.
                if (".name" in key) return@forEach

                val translation = T9nProtocol.text(key, locale)
                localizations[discordLocale] = translation
            }

            localizations
        }
    }
}