package com.crafter.discord.core

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class ConceptCommand<D>(
    open val name: String,
    open val description: String,
    open val setup: (CommandSetup.() -> CommandSetup)? = null
) : ListenerAdapter() {
    abstract val commandData: D

    abstract suspend fun invoke(event: SlashCommandInteractionEvent)

    abstract suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>?
}