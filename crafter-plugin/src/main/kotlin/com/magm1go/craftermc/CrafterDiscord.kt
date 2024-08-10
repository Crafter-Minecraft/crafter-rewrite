package com.magm1go.craftermc

import com.magm1go.craftermc.commands.ReloadConfig
import com.magm1go.craftermc.listeners.ChatListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CrafterDiscord : JavaPlugin() {
    companion object {
        @JvmStatic
        val LOGGER: Logger = LogManager.getLogger("CrafterDiscord")

        @JvmStatic
        fun sendWebhook(url: String, content: String) {
            try {
                val jsonData = JSONObject()

                jsonData["content"] = content

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 OPR/94.0.0.0 (Edition Yx GX)")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData.toString()))
                    .build()

                val client = HttpClient.newHttpClient()
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onEnable() {
        if (!config.contains("webhook-url") || config.getString("webhook-url")!!.isNotEmpty())
            saveDefaultConfig()

        LOGGER.info("Config loaded")

        getCommand("reload")?.setExecutor(ReloadConfig())

        Bukkit.getPluginManager()
            .registerEvents(ChatListener(), this)
    }
}