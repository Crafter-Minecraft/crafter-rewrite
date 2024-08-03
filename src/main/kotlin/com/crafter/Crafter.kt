package com.crafter

import com.crafter.discord.Initializable
import com.crafter.discord.registry.CommandRegistry
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.structure.database.Database
import net.dv8tion.jda.api.JDABuilder

/** Main class singleton **/
object CrafterInstance {
    private val jda = JDABuilder.createDefault(Property("bot.token").getString())
        .build()
    private val initializables: List<Initializable> = listOf(T9nProtocol, Database, CommandRegistry(jda))

    init { initializables.forEach { it.initialize() }}
}

fun main() { CrafterInstance }