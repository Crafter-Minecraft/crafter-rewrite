package com.crafter.protocol.packet.serverbound

import com.crafter.protocol.packet.Packet

class SetCompressionPacket(val threshold: Int, override val packetId: Int = 0x03) : Packet {
    override fun toByteArray(): ByteArray = byteArrayOf()
}