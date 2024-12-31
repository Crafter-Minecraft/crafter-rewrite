package com.crafter.discord.core.child

import com.crafter.discord.core.CommandSetup
import com.crafter.discord.core.ConceptCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

abstract class Subcommand(
    final override val name: String,
    final override val description: String,
    override val setup: (CommandSetup.() -> CommandSetup)? = null,
) : ConceptCommand<SubcommandData>(name, description, setup) {
    var group: SubcommandGroup? = null
        get() = null
        set(value) { field = value }

    override val commandData: SubcommandData = SubcommandData(name, description)

    abstract override suspend fun invoke(event: SlashCommandInteractionEvent)

    fun build() {
        commandData.apply {
            val data = CommandSetup().apply { setup?.invoke(this) }

            if (data.optionContainer.isNotEmpty()) addOptions(data.optionContainer)
        }
    }
}