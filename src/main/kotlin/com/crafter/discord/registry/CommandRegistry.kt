package com.crafter.discord.registry

import com.crafter.implementation.pingCommandInstance
import com.crafter.implementation.rcon.rconCommandInstance

object CommandRegistry {
    val slashCommandList = listOf(
        pingCommandInstance,
        rconCommandInstance
    )
}