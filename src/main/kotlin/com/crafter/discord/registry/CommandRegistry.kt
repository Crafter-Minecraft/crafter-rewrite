package com.crafter.discord.registry

import com.crafter.discord.Initializable
import com.crafter.implementation.PingCommand
import com.crafter.implementation.rcon.RconCommand
import com.crafter.structure.utilities.getDefaultScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandRegistry(private val jda: JDA) : ListenerAdapter(), Initializable {
    private val defaultScope = getDefaultScope()
    private val slashCommandList = listOf(
        RconCommand,
        PingCommand
    )

    // TODO: Make it global after release
    override fun onReady(event: ReadyEvent) {
        val guild = jda.getGuildById(1069511383974166578)!!
        guild
            .updateCommands()
            .addCommands(slashCommandList.map { it.commandData })
            .queue()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        defaultScope.launch {
            slashCommandList
                .firstOrNull { it.name == event.name }
                ?.execute(event)
        }
    }

    override fun initialize() {
        jda.addEventListener(this)
    }
}