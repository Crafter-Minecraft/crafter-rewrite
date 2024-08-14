package com.crafter.implementation

import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.text
import com.crafter.implementation.listeners.ReactionListener
import com.crafter.structure.database.repositories.BridgeRepository
import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.structure.minecraft.Color
import com.crafter.structure.minecraft.Formatting
import com.crafter.structure.minecraft.rcon.RconController
import com.crafter.structure.utilities.getDefaultScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.time.Duration.Companion.seconds

object BridgeCommand : SlashCommand(
    "bridge",
    "Makes bridge between your minecraft server and discord"
) {
    private const val WHITE_CHECK_MARK = "U+2705"
    private val WHITE_CHECK_MARK_EMOJI = Emoji.fromUnicode(WHITE_CHECK_MARK)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "enable" -> {
                var enabled = BridgeRepository.isEnabled(event.guild!!.id)

                val isRconEnabled = RCONRepository.get(event.guild!!.id) != null
                if (!isRconEnabled && !enabled) {
                    event.reply(text(
                        "bridge.rcon_not_enabled",
                        "Sorry, but bridge can't work without RCON. Setup RCON and try again.",
                        event.userLocale
                    )).queue()
                    return
                }

                val data = BridgeRepository.get(event.guild!!.id)
                val toUpsert = mutableMapOf<String, Any>("guildId" to event.guild!!.id, "isEnabled" to enabled)

                if (data != null) {
                    toUpsert["isEnabled"] = !enabled
                    toUpsert["channelId"] = data["channelId"].toString()
                }

                enabled = toUpsert["isEnabled"] as Boolean

                BridgeRepository.upsert(toUpsert)

                val responseText = text(
                    "bridge.toggled",
                    "Bridge was %s",
                    event.userLocale
                ).format(
                    text(
                        "bridge.toggle.value.${if (enabled) "enabled" else "disabled"}",
                        if (enabled) "enabled" else "disabled",
                        event.userLocale
                    )
                )

                event.reply(responseText).queue()
            }
            "channel" -> {
                val repository = BridgeRepository
                val guildId = event.guild!!.id
                val data = repository.get(guildId)
                val channelId = event.getOption("channel")!!.asChannel.id

                if (data != null && data["channelId"] == channelId) {
                    repository.delete(guildId)
                    event.reply(text(
                        "bridge.channel.deleted",
                        "Channel was deleted as bridge.",
                        event.userLocale
                    )).queue()
                    return
                }

                repository.upsert(mapOf(
                    "guildId" to event.guild!!.id,
                    "isEnabled" to true,
                    "channelId" to channelId
                ))

                event.reply(text("bridge.channel.updated", "Channel was set.", event.userLocale)).queue()
            }
        }
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    override fun onMessageReceived(event: MessageReceivedEvent) {
        getDefaultScope().launch {
            if (event.author.isBot) return@launch

            val repository = BridgeRepository

            if (repository.isEnabled(event.guild.id)) {
                val data = repository.get(event.guild.id) ?: return@launch
                val channelId = data["channelId"]

                if (channelId == event.channel.id) {
                    val rconData = RCONRepository.get(event.guild.id) ?: return@launch
                    val listener = ReactionListener(event.channel.id, event.author.id, WHITE_CHECK_MARK_EMOJI) {
                        RconController(
                            rconData["ip"].toString(),
                            rconData["port"] as Int,
                            RCONRepository.getRconPassword(rconData["password"].toString())
                        ).use {
                            var command =
                                "tellraw @a \"${Color.BLUE.code}[Discord]${Formatting.RESET_ADD_COLOR.code} ${event.message.author.name}: ${event.message.contentDisplay} %s\""

                            command = if (event.message.attachments.isNotEmpty()) {
                                command.format("[attachments (${event.message.attachments.size})]")
                            } else {
                                command.format("")
                            }

                            it.send(command)

                            event.message.clearReactions().queue()
                        }
                    }

                    event.message.addReaction(WHITE_CHECK_MARK_EMOJI).queue()
                    listener.register(event.jda)

                    withTimeoutOrNull(5.seconds) {
                        listener.await()
                    } ?: clearReactionsAndUnregister(listener, event.message)
                }
            }
        }
    }

    private fun clearReactionsAndUnregister(listener: ReactionListener, message: Message) {
        message.clearReactions().queue()
        listener.unregister(message.jda)
    }

    init {
        commandData.addSubcommands(
            SubcommandData("enable", "Toggle bridge between servers"),
            SubcommandData("channel", "Channel that should be bridge between servers")
                .addOption(OptionType.CHANNEL, "channel", "Channel", true)
        )

        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    }
}