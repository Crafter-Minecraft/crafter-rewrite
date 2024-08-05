package com.crafter.structure.minecraft

enum class Color(val code: String) {
    DARK_BLUE("§1"),
    DARK_GREEN("§2"),
    DARK_AQUA("§3"),
    DARK_RED("§4"),
    DARK_PURPLE("§5"),
    DARK_GRAY("§8"),
    MINECOIN_GOLD("§g"),
    MATERIAL_QUARTZ("§h"),
    MATERIAL_IRON("§i"),
    MATERIAL_NETHERITE("§j"),
    MATERIAL_REDSTONE("§m"),
    MATERIAL_COPPER("§n"),
    MATERIAL_GOLD("§p"),
    MATERIAL_EMERALD("§q"),
    MATERIAL_DIAMOND("§s"),
    MATERIAL_LAPIS("§t"),
    MATERIAL_AMETHYST("§u"),
    GOLD("§6"),
    GRAY("§7"),
    BLUE("§9"),
    GREEN("§a"),
    AQUA("§b"),
    RED("§c"),
    LIGHT_PURPLE("§d"),
    YELLOW("§e"),
    WHITE("§f"),
    BLACK("§0")
}

enum class Formatting(val code: String) {
    OBFUSCATED("§k"),
    BOLD("§l"),
    STRIKETHROUGH("§m"),
    UNDERLINE("§n"),
    ITALIC("§o"),
    RESET("§r"),
    RESET_ADD_COLOR("§r§f")
}