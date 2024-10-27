package com.crafter.discord.commands

import com.crafter.discord.t9n.T9nProtocol
import com.crafter.discord.t9n.text
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import kotlin.collections.HashMap

// TODO: Implement convenient subgroup/command creation
abstract class SlashCommand(
    final override val name: String,
    final override val description: String,
    final override val options: List<OptionData> = emptyList()
) : Command, IExecutableCommand<SlashCommandInteractionEvent>, ListenerAdapter() {
    val commandData = Commands.slash(name, description)

    fun addCommandGroup(vararg groupList: SubcommandGroupData): SlashCommand {
        commandData.addSubcommandGroups(groupList.toList())
        return this
    }

    fun addSubCommand(vararg subCommands: SubcommandData): SlashCommand {
        commandData.addSubcommands(subCommands.toList())
        return this
    }

    fun SlashCommandInteractionEvent.replyWithMessage(
        translationKey: String,
        userLocale: DiscordLocale,
        appendMessage: StringBuilder.() -> Unit = {}
    ) {
        val message = text(translationKey, userLocale) + StringBuilder().apply(appendMessage).toString()
        this.hook.sendMessage(message).queue()
    }

    fun SlashCommandInteractionEvent.replyLocalized(
        translationKey: String,
        userLocale: DiscordLocale,
        appendMessage: StringBuilder.() -> Unit = {}
    ) {
        val message = text(translationKey, userLocale) + StringBuilder().apply(appendMessage).toString()
        this.reply(message).queue()
    }

    fun setup() {
        commandData.setLocalizationFunction { key ->
            val localizations = HashMap<DiscordLocale, String>()

            T9nProtocol.AVAILABLE_LOCALES.forEach { locale ->
                val discordLocale = DiscordLocale.from(locale)

                // Skipping because it's always english.
                if (".name" in key) return@forEach

                val translation = T9nProtocol.text(key, locale)
                localizations[discordLocale] = translation
            }

            localizations
        }

        commandData.addOptions(options)
    }
}