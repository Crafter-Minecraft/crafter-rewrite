package com.crafter.discord.commands

import net.dv8tion.jda.api.events.Event

interface IExecutableCommand<in T : Event> {
    suspend fun execute(event: T)
}