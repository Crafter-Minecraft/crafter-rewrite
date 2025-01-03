package com.crafter.implementation.commands

import com.crafter.Color
import com.crafter.Formatting
import com.crafter.discord.t9n.text
import com.crafter.discord.core.child.SlashCommand
import com.crafter.discord.core.child.Subcommand
import com.crafter.getDefaultScope
import com.crafter.implementation.commands.listeners.ReactionListener
import com.crafter.rcon.RconController
import com.crafter.structure.database.models.Bridge
import com.crafter.structure.database.repositories.BridgeRepository
import com.crafter.structure.database.repositories.RCONRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import kotlin.time.Duration.Companion.seconds

object BridgeCommand : SlashCommand("bridge", "Makes bridge between your minecraft server and discord", {
    defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    subCommand(EnableBridgeCommand::class)
    subCommand(ChannelBridgeCommand::class)
}) {
    private const val WHITE_CHECK_MARK = "U+2705"
    private val WHITE_CHECK_MARK_EMOJI = Emoji.fromUnicode(WHITE_CHECK_MARK)

    override suspend fun invoke(event: SlashCommandInteractionEvent) {}

    override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    override fun onMessageReceived(event: MessageReceivedEvent) {
        getDefaultScope().launch {
            if (event.author.isBot) return@launch

            val repository = BridgeRepository

            if (repository.isEnabled(event.guild.id)) {
                val bridgeData = repository.get(event.guild.id) ?: return@launch

                if (bridgeData.channelId == event.channel.id) {
                    val rconData = RCONRepository.get(event.guild.id) ?: return@launch
                    val listener = ReactionListener(event.channel.id, event.author.id, WHITE_CHECK_MARK_EMOJI) {
                        RconController(
                            rconData.ipv4,
                            rconData.port,
                            RCONRepository.getRconPassword(rconData.password)
                        ).use {
                            var command = "tellraw @a \"${Color.BLUE.code}[Discord]${Formatting.RESET_ADD_COLOR.code} ${event.message.author.name}: ${event.message.contentDisplay} %s\""

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

                    withTimeoutOrNull(5.seconds) { listener.await() } ?: clearReactionsAndUnregister(listener, event.message)
                }
            }
        }
    }

    private fun clearReactionsAndUnregister(listener: ReactionListener, message: Message) {
        message.clearReactions().queue()
        listener.unregister(message.jda)
    }

    object EnableBridgeCommand : Subcommand("enable", "Toggle bridge between servers") {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            val guildId = event.guild!!.id

            var isBridgeEnabled = BridgeRepository.isEnabled(guildId)
            val isRconEnabled = RCONRepository.get(guildId) != null

            if (!isRconEnabled && !isBridgeEnabled) {
                event.replyLocalized(
                    "bridge.rcon_not_enabled",
                    event.userLocale
                )
                return
            }

            val bridgeData = BridgeRepository.get(guildId) ?: return
            val toUpsert = Bridge(
                guildId,
                !isBridgeEnabled,
                bridgeData.channelId
            )

            isBridgeEnabled = toUpsert.bridgeEnable
            BridgeRepository.upsert(toUpsert)

            val statusText = text(
                "bridge.toggle.value.${if (isBridgeEnabled) "enabled" else "disabled"}",
                event.userLocale
            )

            val responseText = text(
                "bridge.toggled",
                event.userLocale
            ).format(statusText)

            event.reply(responseText).queue()
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    object ChannelBridgeCommand : Subcommand("channel", "Channel that should be bridge between servers", {
        argument<TextChannel>("channel", "Channel", true)
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!
            val channel = event.getOption("channel")!!.asChannel as TextChannel

            if (!channel.canTalk()) {
                event.replyLocalized("bridge.channel.not_enough_perms", event.userLocale)
                return
            }

            val repository = BridgeRepository
            val guildId = guild.id
            val data = repository.get(guildId)

            if (data != null && data.channelId == channel.id) {
                repository.delete(guildId)
                event.replyLocalized("bridge.channel.deleted", event.userLocale)
                return
            }

            repository.upsert(
                Bridge(
                    event.guild!!.id,
                    true,
                    channel.id,
                )
            )

            event.replyLocalized("bridge.channel.updated", event.userLocale)
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }
}