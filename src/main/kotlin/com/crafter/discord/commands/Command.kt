package com.crafter.discord.commands

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.commands.build.OptionData

interface Command<in T : Event> {
    val name: String
    val description: String
    val options: List<OptionData>

    suspend fun callback(event: T)
}