package com.crafter.discord.core.child

import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

abstract class SubcommandGroup(
    val name: String,
    val description: String,
    vararg val subcommands: Subcommand
) {
    val groupData = SubcommandGroupData(name, description)

    init {
        subcommands.forEach {
            it.group = this
            it.build()
        }
        groupData.addSubcommands(subcommands.map { it.commandData })
    }
}