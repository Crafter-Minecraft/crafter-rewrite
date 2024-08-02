package com.crafter.structure.minecraft.protocol.packet

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class LoginAcknowledg(override val packetId: Int = 0x03) : Packet {
    override fun toByteArray(): ByteArray {
        val packet = ByteArrayOutputStream()
        val stream = DataOutputStream(packet)

        stream.writeByte(packetId)

        return packet.toByteArray()
    }
}