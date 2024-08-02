package com.crafter.structure.minecraft.protocol.packet

interface Packet {
    val packetId: Int
    fun toByteArray(): ByteArray
}