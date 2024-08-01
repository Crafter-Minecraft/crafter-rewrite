package com.crafter.implementation

import com.crafter.discord.commands.slash
import com.crafter.discord.t9n.text
import com.crafter.structure.minecraft.pinger.Pinger
import com.crafter.structure.utilities.UnstableApi
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.editMessage
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.time.Duration.Companion.seconds

val options = listOf(
    OptionData(OptionType.STRING, "server", "пиши сука", true),
)

@OptIn(UnstableApi::class)
val pingCommandInstance = slash("ping", "Look an info about server", options) { event ->
    event.deferReply().queue()

    val pinger = Pinger()

    withTimeoutOrNull(30.seconds) {
        println("alo?")
        val serverInfo = pinger.getServerInfo("mc.hypixel.net", 25565)
        println(serverInfo)
        event.hook.editMessage(event.interaction.id, serverInfo.toString()).queue()
    } ?: println("ne"); event.hook.editMessage(event.interaction.id, text(
        "ping.cant_retrieve_server",
        "Make sure IP / port is valid. Maybe server offline?",
        event.userLocale
    ))
}