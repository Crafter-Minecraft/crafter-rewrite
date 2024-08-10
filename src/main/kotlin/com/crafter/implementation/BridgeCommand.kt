package com.crafter.implementation

import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.text
import com.crafter.structure.database.repositories.BridgeRepository
import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.structure.minecraft.Color
import com.crafter.structure.minecraft.Formatting
import com.crafter.structure.minecraft.rcon.RconController
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.resolveColumnType

object BridgeCommand : SlashCommand(
    "bridge",
    "Makes bridge between your minecraft server and discord"
) {
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

    override fun onMessageReceived(event: MessageReceivedEvent) = runBlocking {
        if (event.author.isBot) return@runBlocking

        val repository = BridgeRepository

        println(repository.isEnabled(event.guild.id))
        if (repository.isEnabled(event.guild.id)) {
            val data = repository.get(event.guild.id) ?: return@runBlocking
            val channelId = data["channelId"]

            println(channelId)
            println(channelId == event.channel.id)
            if (channelId == event.channel.id) {
                val rconData = RCONRepository.get(event.guild.id) ?: return@runBlocking
                RconController(
                    rconData["ip"].toString(),
                    rconData["port"] as Int,
                    RCONRepository.getRconPassword(rconData["password"].toString())
                ).use {
                    it.send("tellraw @a \"${Color.BLUE.code}[Discord]${Formatting.RESET_ADD_COLOR.code} ${event.message.author.name}: ${event.message.contentDisplay}\"")
                }
            }
        }
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