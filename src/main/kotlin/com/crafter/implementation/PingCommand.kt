package com.crafter.implementation

import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.text
import com.crafter.structure.minecraft.Color
import com.crafter.structure.minecraft.Formatting
import com.crafter.structure.minecraft.protocol.MinecraftProtocol
import com.crafter.structure.minecraft.protocol.packet.handshake.HandshakeState
import com.crafter.structure.utilities.UnstableApi
import com.crafter.structure.utilities.embed
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.messages.MessageCreateData

@OptIn(UnstableApi::class)
object PingCommand : SlashCommand(
    "ping",
    "ping.description",
    listOf(
        OptionData(OptionType.STRING, "server", "Server that you wanna check", true),
        OptionData(OptionType.STRING, "port", "Server port", false),
        OptionData(OptionType.STRING, "version", "Server version", false)
    )
) {
    // TODO: Move it to minecraft and add more versions
    private val protocolVersionMap = mapOf(
        "1.8" to 47,
        "1.12.2" to 340,
        "1.19.3" to 761
    )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        val address = event.getOption("server")!!.asString
        val port = event.getOption("port")?.asInt ?: 25565
        val version = event.getOption("version")?.asString ?: "1.12.2"
        val protocolVersion = protocolVersionMap[version] ?: 340

        try {
            val serverInfo = getServerInfo(address, port, protocolVersion)
            val description = parseDescriptionText(
                serverInfo["description"],
                event.userLocale
            )

            println(description)

            event.hook.sendMessage(MessageCreateData.fromEmbeds(embed {
                setTitle("Server Info")
                setDescription(description)
            })).queue()
        } catch (e: Exception) {
            e.printStackTrace()
            event.hook.sendMessage(
                text(
                    "ping.cant_retrieve_server",
                    "Make sure IP / port is valid. Other possible issues:\n" +
                            "1. Invalid version\n" +
                            "2. Server is offline",
                    event.userLocale
                )
            ).queue()
        }
    }

    private suspend fun getServerInfo(address: String, port: Int, protocolVersion: Int): JsonObject {
        val rawInfo = MinecraftProtocol(address, port).use {
            it.sendHandshake(protocolVersion, HandshakeState.State)
        }

        return Json.decodeFromString(rawInfo)
    }

    private fun parseDescriptionText(serverInfo: JsonElement?, locale: DiscordLocale): String {
        if (serverInfo == null) return text("ping.no_description", "Server has no description", locale)

        val textBuilder = StringBuilder()
            .append(" ")
        when (serverInfo) {
            is JsonObject -> {
                for ((key, value) in serverInfo) {
                    if (key == "text") {
                        textBuilder.append(value.jsonPrimitive.content)
                    } else {
                        textBuilder.append(parseDescriptionText(value, locale))
                    }
                }
            }
            is JsonArray -> {
                for (element in serverInfo) {
                    textBuilder.append(parseDescriptionText(element, locale))
                }
            }
            is JsonPrimitive -> {
                if (serverInfo.isString) {
                    textBuilder.append(serverInfo.content)
                }
            }
        }

        var text = textBuilder.toString()
        Color.entries.forEach { entry ->
            text = text.replace(entry.code, "")
            text = text.replace(entry.name.lowercase(), "")
        }
        Formatting.entries.forEach { entry ->
            text = text.replace(entry.code, "")
        }
        return text
    }
}