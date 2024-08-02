package com.crafter.implementation

import com.crafter.discord.commands.slash
import com.crafter.discord.t9n.text
import com.crafter.structure.minecraft.protocol.MinecraftProtocol
import com.crafter.structure.minecraft.protocol.packet.handshake.HandshakeState
import com.crafter.structure.utilities.UnstableApi
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.seconds

val options = listOf(
    OptionData(OptionType.STRING, "server", "ping.server.description", true),
    OptionData(OptionType.STRING, "port", "ping.port.description", false),
    OptionData(OptionType.STRING, "version", "ping.version.description", false),
)
val protocolVersionMap = mapOf(
    "1.8" to 47,
    "1.12.2" to 340,
    "1.19.3" to 761
)

@OptIn(UnstableApi::class)
val pingCommandInstance = slash("ping", "ping.description", options) { event ->
    event.deferReply().queue()

    val address = event.interaction.getOption("server")!!.asString
    val port = event.interaction.getOption("port")?.asInt ?: 25565
    val version = event.interaction.getOption("version")?.asString ?: "1.12.2"
    val protocolVersion = protocolVersionMap[version] ?: 340

    try {
        val serverInfo = MinecraftProtocol(address, port).use {
            it.sendHandshake(protocolVersion, HandshakeState.State)
        }

        event.hook.send(serverInfo).queue()
    } catch (e: Exception) {
        event.hook.send(text(
            "ping.cant_retrieve_server",
            "Make sure IP / port is valid. Other possible issues:\n" +
                    "1. Invalid version\n" +
                    "2. Server is offline",
            event.userLocale
        )).queue()
    }
}