package com.crafter.discord.commands

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

interface IExecutableCommand<in T : Event> {
    suspend fun execute(event: T)
    fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>?
}