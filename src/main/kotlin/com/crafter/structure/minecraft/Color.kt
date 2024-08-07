package com.crafter.structure.minecraft

const val PARAGRAPH = "§"

enum class Color(val code: String) {
    DARK_BLUE("${PARAGRAPH}1"),
    DARK_GREEN("${PARAGRAPH}2"),
    DARK_AQUA("${PARAGRAPH}3"),
    DARK_RED("${PARAGRAPH}4"),
    DARK_PURPLE("${PARAGRAPH}5"),
    DARK_GRAY("${PARAGRAPH}8"),
    MINECOIN_GOLD("${PARAGRAPH}g"),
    MATERIAL_QUARTZ("${PARAGRAPH}h"),
    MATERIAL_IRON("${PARAGRAPH}i"),
    MATERIAL_NETHERITE("${PARAGRAPH}j"),
    MATERIAL_REDSTONE("${PARAGRAPH}m"),
    MATERIAL_COPPER("${PARAGRAPH}n"),
    MATERIAL_GOLD("${PARAGRAPH}p"),
    MATERIAL_EMERALD("${PARAGRAPH}q"),
    MATERIAL_DIAMOND("${PARAGRAPH}s"),
    MATERIAL_LAPIS("${PARAGRAPH}t"),
    MATERIAL_AMETHYST("${PARAGRAPH}u"),
    GOLD("${PARAGRAPH}6"),
    GRAY("${PARAGRAPH}7"),
    BLUE("${PARAGRAPH}9"),
    GREEN("${PARAGRAPH}a"),
    AQUA("${PARAGRAPH}b"),
    RED("${PARAGRAPH}c"),
    LIGHT_PURPLE("${PARAGRAPH}d"),
    YELLOW("${PARAGRAPH}e"),
    WHITE("${PARAGRAPH}f"),
    BLACK("${PARAGRAPH}0")
}

enum class Formatting(val code: String) {
    OBFUSCATED("${PARAGRAPH}k"),
    BOLD("${PARAGRAPH}l"),
    STRIKETHROUGH("${PARAGRAPH}m"),
    UNDERLINE("${PARAGRAPH}n"),
    ITALIC("${PARAGRAPH}o"),
    RESET("${PARAGRAPH}r"),
    RESET_ADD_COLOR("${PARAGRAPH}r${PARAGRAPH}f")
}

fun clearText(text: String): String {
    return text.replace("${PARAGRAPH}.".toRegex(), "")
}