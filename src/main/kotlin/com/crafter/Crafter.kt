package com.crafter

import com.crafter.discord.registry.CommandRegistry
import com.crafter.discord.t9n.T9nProtocol
import com.crafter.structure.database.Database
import com.crafter.structure.minecraft.pinger.PingPacket
import com.crafter.structure.minecraft.pinger.Pinger
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.default
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent

/** Main class singleton **/
@OptIn(DelicateCoroutinesApi::class)
object CrafterInstance {
    private val initializables = listOf(T9nProtocol, Database)
    private val jda by lazy {
        default(Property("bot.token").getString(), enableCoroutines = true)
    }

    private val catchCoroutineExceptions = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        initializables.forEach { it.initialize() }

        jda.listener<SlashCommandInteractionEvent> { event ->
            GlobalScope.launch(catchCoroutineExceptions) {
                CommandRegistry.slashCommandList
                    .firstOrNull { it.name == event.name }
                    ?.callback(event)
            }
        }

        // TODO: Make it global after release
        jda.listener<ReadyEvent> {
            val guild = jda.getGuildById(1069511383974166578)
            CommandRegistry.slashCommandList.map {
                guild
                    ?.upsertCommand(it.get())
                    ?.queue()
            }
        }
    }
}

fun main() { CrafterInstance }