package com.crafter.discord.registry

import com.crafter.discord.Initializable
import com.crafter.implementation.PingCommand
import com.crafter.implementation.rcon.RconCommand
import com.crafter.structure.utilities.capitalize
import com.crafter.structure.utilities.getDefaultScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandRegistry(private val jda: JDA) : ListenerAdapter(), Initializable {
    private val defaultScope = getDefaultScope()
    private val slashCommandList = listOf(
        RconCommand,
        PingCommand
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
                    val options = it.second.filter { word -> word.startsWith(event.focusedOption.value.capitalize()) }.apply {
                        dropLastWhile { this.size > 25 }
                    }

                    event.replyChoiceStrings(options).queue()
                }
            }
        }
    }

    override fun initialize() {
        jda.addEventListener(this)

        val guild = jda.getGuildById(1069511383974166578)!!
        guild
            .updateCommands()
            .addCommands(slashCommandList.map { it.commandData })
            .queue()
    }
}