package com.crafter.structure.minecraft.protocol.packet

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class RequestPacket(override val packetId: Int) : Packet {
    override fun toByteArray(): ByteArray {
        val request = ByteArrayOutputStream()
        val requestStream = DataOutputStream(request)
        requestStream.writeByte(packetId)

        return request.toByteArray()
    }
}