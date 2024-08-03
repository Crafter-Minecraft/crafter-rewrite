package com.crafter.discord.registry

import com.crafter.implementation.PingCommand
import com.crafter.implementation.rcon.RconCommand

object CommandRegistry {
    val slashCommandList = listOf(
        RconCommand,
        PingCommand
    )
}