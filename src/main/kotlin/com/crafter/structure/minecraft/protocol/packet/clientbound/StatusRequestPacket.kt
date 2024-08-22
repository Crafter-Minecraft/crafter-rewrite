package com.crafter.structure.minecraft.protocol.packet.clientbound

import com.crafter.structure.minecraft.protocol.packet.Packet
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class StatusRequestPacket(override val packetId: Int = 0x00) : Packet {
    class Legacy(override val packetId: Int = 0xFE) : Packet {
        override fun toByteArray(): ByteArray {
            val out = ByteArrayOutputStream()
            val stream = DataOutputStream(out)

            stream.writeByte(packetId) // Packet ID for legacy ping
            stream.writeByte(0x01) // Payload (always 1)

            return out.toByteArray()
        }
    }

    override fun toByteArray(): ByteArray {
        val request = ByteArrayOutputStream()
        val requestStream = DataOutputStream(request)
        requestStream.writeByte(packetId)

        return request.toByteArray()
    }
}