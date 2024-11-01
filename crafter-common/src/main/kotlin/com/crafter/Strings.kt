package com.crafter

import java.util.*

fun String.capitalize(): String = this.replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase(
        Locale.getDefault()
    ) else char.toString()
}