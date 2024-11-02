package com.crafter

import com.crafter.discord.Initializable
import com.crafter.discord.registry.CommandRegistry
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.structure.database.Database
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import top.boticord.boticord

/** Main class singleton **/
object CrafterInstance {
    private val boticordChannelId by Property("bot.boticord.channel")
    private val botToken by Property("bot.token")
    private val boticordToken by Property("bot.boticord.token")

    private val jda = JDABuilder.create(
        botToken,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT,
        GatewayIntent.GUILD_MESSAGE_REACTIONS
    ).build()

    private val initializables: List<Initializable> = listOf(
        T9nProtocol,
        Database,
        CommandRegistry(jda)
    )

    init {
        jda.awaitReady()

        initializables.forEach { it.initialize() }

        if (boticordToken.isNotEmpty()) {
            val boticordChannel = jda.getTextChannelById(boticordChannelId)

            getDefaultScope().launch {
                boticord(token = boticordToken) {
                    autopost(jda.selfUser.idLong) {
                        guilds(jda.guilds.size)
                        shards(jda.shardInfo.shardTotal)
                    }
                    notifications { event ->
                        val eventName = event.event

                        if (eventName == "pong") return@notifications

                        boticordChannel?.sendMessage(
                            "Boticord Event Name: ${eventName}:\n" +
                                    "ID: ${event.data.id}\n" +
                                    "Type: ${event.data.type}\n" +
                                    "User: ${event.data.user}\n" +
                                    "Payload: ${event.data.payload}\n" +
                                    "Happened: ${event.data.happened}\n"
                        )?.queue()
                    }
                }
            }
        }
    }
}

fun main() { CrafterInstance }
