package com.crafter.discord.registry

import com.crafter.discord.Initializable
import com.crafter.implementation.BridgeCommand
import com.crafter.implementation.PingCommand
import com.crafter.implementation.RconCommand
import com.crafter.implementation.bot.BotCommand
import com.crafter.structure.utilities.getDefaultScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandRegistry(private val jda: JDA) : ListenerAdapter(), Initializable {
    private val defaultScope = getDefaultScope()
    private val slashCommandList = listOf(
        PingCommand,
        RconCommand,
        BridgeCommand,
    )

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        defaultScope.launch {
            slashCommandList
                .firstOrNull { it.name == event.name }
                ?.execute(event)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        defaultScope.launch {
            val command = slashCommandList.firstOrNull { it.name == event.name }

            val autoCompleteOptions = command?.autoComplete(event)
            autoCompleteOptions?.forEach {
                if (event.name == command.name && it.first == event.focusedOption.name) {
                    val options = it.second.filter { word -> word.startsWith(event.focusedOption.value) }

                    event.replyChoiceStrings(options.subList(0, if (options.size > 25) 25 else options.size)).queue()
                }
            }
        }
    }

    override fun initialize() {
        jda.addEventListener(this)

        slashCommandList.forEach { command ->
            command.setup()

            jda.addEventListener(command)
        }

        jda.updateCommands()
            .addCommands(slashCommandList.map { it.commandData })
            .queue()
    }
}