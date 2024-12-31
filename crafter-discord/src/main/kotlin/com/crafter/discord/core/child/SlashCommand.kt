package com.crafter.discord.core.child

import com.crafter.discord.t9n.text
import com.crafter.discord.core.CommandSetup
import com.crafter.discord.core.ConceptCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class SlashCommand(
    final override val name: String,
    final override val description: String,
    final override val setup: (CommandSetup.() -> CommandSetup)? = null
) : ConceptCommand<SlashCommandData>(name, description, setup) {
    val subgroups = mutableListOf<SubcommandGroup>()
    val subcommands = mutableListOf<Subcommand>()

    final override val commandData: SlashCommandData = Commands.slash(name, description)

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

    fun build() {
        commandData.apply {
            val data = CommandSetup().apply { setup?.invoke(this) }

            when {
                data.optionContainer.isNotEmpty() -> addOptions(data.optionContainer)
                data.subCommandContainer.isNotEmpty() -> {
                    data.subCommandContainer.keys.forEach { subCommand ->
                        this@SlashCommand.subcommands.add(subCommand)
                        subCommand.build()
                    }

                    addSubcommands(data.subCommandContainer.values)
                }
                data.subCommandGroupContainer.isNotEmpty() -> {
                    data.subCommandGroupContainer.keys.forEach { subCommandGroup ->
                        this@SlashCommand.subgroups.add(subCommandGroup)
                    }

                    addSubcommandGroups(data.subCommandGroupContainer.values)
                }
            }

            defaultPermissions = data.defaultPermissions
        }
    }
}