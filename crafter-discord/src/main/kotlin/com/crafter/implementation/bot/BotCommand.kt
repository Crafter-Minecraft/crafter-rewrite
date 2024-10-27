package com.crafter.implementation.bot

import com.crafter.discord.commands.SlashCommand
import com.crafter.structure.utilities.annotations.UnstableApi
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import protocol.MinecraftProtocol
import protocol.ProtocolVersion
import protocol.packet.clientbound.handshake.HandshakeState
import java.util.*

object BotCommand : SlashCommand("bot", "Your minecraft bot") {
    private const val BOT_NAME: String = "crafter"
    private val uuid: UUID = UUID.randomUUID()

    @OptIn(UnstableApi::class)
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        MinecraftProtocol("localhost",25565).use {
            it.sendHandshake(ProtocolVersion.V1_21, HandshakeState.Login)
            println(it.sendLoginStart(
                ProtocolVersion.V1_21,
                BOT_NAME,
                null,
                uuid
            ))
            // it.sendPacket(LoginAcknowledgePacket())
        }
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
}