package com.crafter.implementation

import com.crafter.annotations.UnstableApi
import com.crafter.clearText
import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.text
import com.crafter.structure.utilities.Images
import com.crafter.structure.utilities.embed
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import com.crafter.protocol.MinecraftProtocol
import com.crafter.protocol.ProtocolVersion
import com.crafter.protocol.getByProtocolVersion
import com.crafter.protocol.packet.clientbound.handshake.HandshakeState

@OptIn(UnstableApi::class)
object PingCommand : SlashCommand(
    "ping",
    "Ping server you want",
    listOf(
        OptionData(OptionType.STRING, "server", "Server that you wanna check", true),
        OptionData(OptionType.STRING, "port", "Server port", false),
        OptionData(OptionType.STRING, "version", "Server version (eg. 1.12.2; Alpha 1.2.2; Beta 1.1_02)", false, true)
    )
) {
    private val protocolVersionMap = ProtocolVersion.entries.associateBy({ it.original.lowercase() }, { it.number })
    private val versionProtocolMap = ProtocolVersion.entries.associateBy({ it.number }, { it.original })

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        val address = event.getOption("server")!!.asString
        val port = event.getOption("port")?.asInt ?: 25565
        val version = event.getOption("version")?.asString ?: "1.12.2"
        // We lowercasing it because it can be "Alpha 1.2.2", "Beta 1.1_02" etc.
        val protocolVersion = protocolVersionMap[version.lowercase()]

        if (protocolVersion == null) {
            event.replyWithMessage(
                "ping.version_not_found",
                event.userLocale
            )
            return
        }

        try {
            val serverInfo = getServerInfo(address, port, protocolVersion)
            val messageBuilder = MessageCreateBuilder()
                .setEmbeds(serverInfoEmbed(serverInfo, event.userLocale))

            val favicon = serverInfo["favicon"]?.jsonPrimitive?.contentOrNull

            if (favicon != null) {
                val faviconFile = Images.decodeBase64ToFile(favicon, "favicon.png")
                messageBuilder.setFiles(FileUpload.fromData(faviconFile))

                faviconFile.delete()
            }

            event.hook.sendMessage(messageBuilder.build()).queue()
        } catch (e: Exception) {
            e.printStackTrace()
            event.replyWithMessage(
                "ping.cant_retrieve_server",
                event.userLocale
            )
        }
    }

    private suspend fun getServerInfo(address: String, port: Int, protocolVersion: Int): JsonObject {
        /* MinecraftProtocol(address, port).use {
            println(it.sendLegacyPing())
        } */
        val rawInfo = MinecraftProtocol(address, port).use {
            it.sendHandshake(getByProtocolVersion(protocolVersion), HandshakeState.State)
        }

        return Json.decodeFromString(rawInfo)
    }

    private fun parseDescriptionText(previousKey: String? = null, serverInfo: JsonElement?, locale: DiscordLocale): String {
        if (serverInfo == null) return text("ping.no_description", locale)

        val textBuilder = StringBuilder()
            .append(" ")
        when (serverInfo) {
            is JsonObject -> {
                for ((key, value) in serverInfo) {
                    if (key == "text") {
                        textBuilder.append(value.jsonPrimitive.content)
                    } else {
                        textBuilder.append(parseDescriptionText(key, value, locale))
                    }
                }
            }
            is JsonArray -> {
                for (element in serverInfo) {
                    textBuilder.append(parseDescriptionText(previousKey, element, locale))
                }
            }
            is JsonPrimitive -> {
                if (serverInfo.isString && previousKey == "text") {
                    textBuilder.append(serverInfo.content)
                }
            }
        }

        return clearText(textBuilder.toString())
    }

    private fun parseLegacyServerDescription(serverInfo: String): String {
        TODO("Make old description parser")
    }

    private fun serverInfoEmbed(
        serverInfo: JsonObject,
        locale: DiscordLocale
    ) = embed {
        setTitle(text("ping.server_info.title", locale))

        val serverDescription = parseDescriptionText(
            null,
            serverInfo["description"],
            locale
        )

        setDescription(serverDescription)

        val serverVersion = serverInfo["version"]?.jsonObject
        if (serverVersion != null) {
            val protocol = serverVersion["com/crafter/protocol"]?.jsonPrimitive?.content
            val versionByProtocol = versionProtocolMap[protocol?.toInt()] ?: text("ping.server_info.version.unknown", locale)

            addField(
                text("ping.server_info.version", locale),
                "Brand: ${serverVersion["name"]?.jsonPrimitive?.content} " +
                        "| Protocol: $protocol (${versionByProtocol})",
                false
            )
        }

        val playerData = serverInfo["players"]?.jsonObject
        if (playerData != null) {
            addField(
                text("ping.server_info.players", locale),
                "${text("ping.server_info.players.max", locale)}: ${playerData["max"]}\n" +
                        "${text("ping.server_info.players.online", locale)}: ${playerData["online"]}",
                false
            )
        }

        val favicon = serverInfo["favicon"]?.jsonPrimitive?.contentOrNull
        if (favicon != null) setThumbnail("attachment://favicon.png")
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>> {
        // Why kotlin.text.String.capitalize() deprecated?
        /**
        * Here I'm using my extension for string, see [capitalize]
        **/
        return listOf("version" to protocolVersionMap.map { it.key.capitalize() })
    }
}