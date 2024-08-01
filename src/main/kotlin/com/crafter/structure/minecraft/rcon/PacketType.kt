package com.crafter.structure.minecraft.rcon

enum class PacketType(val type: Int) {
    SERVERDATA_RESPONSE_VALUE(0),
    SERVERDATA_EXECCOMMAND(2),
    SERVERDATA_AUTH_RESPONSE(2),
    SERVERDATA_AUTH(3)
}