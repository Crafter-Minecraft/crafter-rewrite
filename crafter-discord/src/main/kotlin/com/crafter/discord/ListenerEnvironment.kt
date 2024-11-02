package com.crafter.discord

import net.dv8tion.jda.api.JDA

interface ListenerEnvironment {
    fun register(jda: JDA)
    fun unregister(jda: JDA)
}