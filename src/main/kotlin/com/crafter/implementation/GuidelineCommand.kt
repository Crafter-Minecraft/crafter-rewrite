package com.crafter.implementation

import com.crafter.discord.commands.SlashCommand
import com.crafter.discord.t9n.T9nProtocol
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object GuidelineCommand : SlashCommand("guideline", "Help with commands and bridge.") {
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subcommandName = event.subcommandName ?: return

        if (subcommandName == "support") {
            event.reply("https://discord.gg/vmQhvvjtvB").queue()
            return
        }

        event.reply(getGuidelineText(subcommandName, event.userLocale)).queue()
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    private fun getGuidelineText(guideline: String, locale: DiscordLocale) =
        T9nProtocol.text("guideline.$guideline.text", locale.locale)

    init {
        commandData.addSubcommands(
            SubcommandData("support", "Link to the support server"),
            SubcommandData("bridge", "Manual to help with bridge."),
            SubcommandData("rcon", "Setting RCON help.")
        )
    }
}