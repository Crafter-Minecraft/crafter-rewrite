package com.magm1go.craftermc.listeners

import com.magm1go.craftermc.CrafterDiscord
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ChatListener : Listener {
    private fun getWebhook(): Any? {
        val config = Bukkit.getPluginManager().getPlugin("CrafterDiscord")!!.config
        return config.get("webhook-url")
    }

    @EventHandler
    private fun onMessage(event: AsyncChatEvent) {
        if (getWebhook() != null) {
            val message = event.message() as TextComponent

            CrafterDiscord.sendWebhook(getWebhook().toString(), "`${event.player.name}`: ${message.content()}")
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        if (getWebhook() != null) {
            CrafterDiscord.sendWebhook(getWebhook().toString(), "**${event.player.name}** зашёл на сервер!")
        }
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        if (getWebhook() != null) {
            CrafterDiscord.sendWebhook(getWebhook().toString(), "**${event.player.name}** вышел с сервера!")
        }
    }
}