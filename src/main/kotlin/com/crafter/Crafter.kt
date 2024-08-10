package com.crafter

import com.crafter.discord.Initializable
import com.crafter.discord.registry.CommandRegistry
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.structure.database.Database
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

/** Main class singleton **/
object CrafterInstance {
    private val jda = JDABuilder.create(
        Property("bot.token").getString(),
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT
    )
        .build()
    private val initializables: List<Initializable> = listOf(T9nProtocol, Database, CommandRegistry(jda))

    init {
        jda.awaitReady()

        initializables.forEach { it.initialize() }
    }
}

fun main() { CrafterInstance }