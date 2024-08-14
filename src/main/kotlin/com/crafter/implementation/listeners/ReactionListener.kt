package com.crafter.implementation.listeners

import com.crafter.structure.utilities.getDefaultScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ReactionListener(
    private val channelId: String,
    private val authorId: String,
    private val emoji: Emoji,
    private val block: suspend () -> Unit
) : ListenerAdapter() {
    private val reactionReceived = CompletableDeferred<Unit>()

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        val clickedUsers = event.reaction.retrieveUsers()
        if (event.channel.id == channelId && event.reaction.emoji == emoji && clickedUsers.any { it.id == authorId }) {
            getDefaultScope().launch { block() }
            reactionReceived.complete(Unit)
            unregister(event.jda)
        }
    }

    suspend fun await() = reactionReceived.await()

    fun register(jda: JDA) = jda.addEventListener(this)
    fun unregister(jda: JDA) = jda.removeEventListener(this)
}