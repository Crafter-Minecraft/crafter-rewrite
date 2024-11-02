package com.crafter.structure.utilities

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

inline fun embed(builder: EmbedBuilder.() -> Unit): MessageEmbed =
    EmbedBuilder()
        .apply(builder)
        .build()

inline fun embedBuilder(builder: EmbedBuilder.() -> Unit): EmbedBuilder =
    EmbedBuilder()
        .apply(builder)