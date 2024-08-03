package com.crafter.discord.commands

import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface Command {
    val name: String
    val description: String
    val options: List<OptionData>

    fun buildOptions(): SlashCommandData
}