package com.crafter.implementation

import com.crafter.discord.ListenerEnvironment
import com.crafter.discord.t9n.text
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.buttons.Button

class Navigation(
    private val issuedChannelId: Long,
    private val authorId: Long,
    private val pages: List<EmbedBuilder>,
    private val locale: DiscordLocale
) : ListenerAdapter(), ListenerEnvironment {
    companion object {
        private val STATIC_BUTTON_IDS = mapOf(
            "first" to "<<",
            "previous" to "<",
            "next" to ">",
            "last" to ">>"
        )
    }

    val row = STATIC_BUTTON_IDS.map { (key, value) -> Button.primary(key, value) }

    private var activePageIndex: Int = 0

    val builtEmbeds = pages.mapIndexed { index, embedBuilder ->
        embedBuilder.setFooter("${text("mod.embed.footer.page", locale)}: ${index + 1}/${pages.size}")
        embedBuilder.build()
    }

    private fun next() {
        if (activePageIndex >= pages.lastIndex) {
            activePageIndex = 0
            return
        }

        activePageIndex++
    }

   private  fun previous() {
        if (activePageIndex <= 0) {
            activePageIndex = pages.lastIndex
            return
        }

        activePageIndex--
    }

    private fun first() {
        if (activePageIndex == 0) {
            last()
            return
        }

        activePageIndex = 0
        pages[activePageIndex]
    }

    private fun last() {
        if (activePageIndex == pages.lastIndex) {
            first()
            return
        }

        activePageIndex = pages.lastIndex
        pages[activePageIndex]
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val componentId = event.componentId

        if (
            event.channel.idLong == issuedChannelId &&
            componentId in STATIC_BUTTON_IDS.keys &&
            event.interaction.user.idLong == authorId
        ) {
            when (componentId) {
                "first" -> first()
                "previous" -> previous()
                "next" -> next()
                "last" -> last()
            }

            event.editMessageEmbeds(builtEmbeds[activePageIndex]).queue()
        }
    }

    override fun register(jda: JDA) = jda.addEventListener(this)

    override fun unregister(jda: JDA) = jda.removeEventListener(this)
}