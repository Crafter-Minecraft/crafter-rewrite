package com.crafter.implementation.commands

import com.crafter.discord.t9n.T9nProtocol
import com.crafter.discord.core.child.SlashCommand
import com.crafter.discord.core.child.Subcommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale

object GuidelineCommand : SlashCommand("guideline", "Help with commands and bridge.", {
    subCommand(SupportGuidelineCommand::class)
    subCommand(RconGuidelineCommand::class)
    subCommand(BridgeGuidelineCommand::class)
}) {
    override suspend fun invoke(event: SlashCommandInteractionEvent) {
        val subcommandName = event.subcommandName ?: return

        event.reply(getGuidelineText(subcommandName, event.userLocale)).queue()
    }

    override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    private fun getGuidelineText(guideline: String, locale: DiscordLocale) =
        T9nProtocol.text("guideline.$guideline.text", locale.locale)

    object BridgeGuidelineCommand : Subcommand("bridge", "Manual to help with bridge.") {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.reply(getGuidelineText(name, event.userLocale)).queue()
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    object SupportGuidelineCommand : Subcommand("support", "Link to the support server") {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.reply("https://discord.gg/vmQhvvjtvB").queue()
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    object RconGuidelineCommand : Subcommand("rcon", "Setting RCON help.") {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.reply(getGuidelineText(name, event.userLocale)).queue()
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }
}