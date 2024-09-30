package com.crafter.structure.utilities

import net.dv8tion.jda.api.EmbedBuilder

inline fun embed(builder: EmbedBuilder.() -> Unit) =
    EmbedBuilder()
        .apply(builder)
        .build()