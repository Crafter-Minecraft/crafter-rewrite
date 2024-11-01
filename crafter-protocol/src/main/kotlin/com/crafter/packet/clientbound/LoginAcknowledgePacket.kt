package com.crafter.protocol.packet.clientbound

import com.crafter.protocol.packet.Packet
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class LoginAcknowledgePacket(override val packetId: Int = 0x03) : Packet {
    override fun toByteArray(): ByteArray {
        val packet = ByteArrayOutputStream()
        val stream = DataOutputStream(packet)

        stream.writeByte(packetId)

        return packet.toByteArray()
    }
}