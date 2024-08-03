package com.crafter

import com.crafter.discord.registry.CommandRegistry
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.structure.database.Database
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/** Main class singleton **/
@OptIn(DelicateCoroutinesApi::class)
object CrafterInstance : ListenerAdapter() {
    private val initializables = listOf(T9nProtocol, Database)
    private val jda = JDABuilder.createDefault(Property("bot.token").getString())
        .addEventListeners(this)
        .build()

    // TODO: Make it global after release
    override fun onReady(event: ReadyEvent) {
        val guild = jda.getGuildById(1069511383974166578)
        CommandRegistry.slashCommandList.map {
            it.buildOptions()

            guild
                ?.upsertCommand(it.instance)
                ?.queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        GlobalScope.launch {
            CommandRegistry.slashCommandList
                .firstOrNull { it.name == event.name }
                ?.execute(event)
        }
    }

    init { initializables.forEach { it.initialize() }}
}

fun main() { CrafterInstance }