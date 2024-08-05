package com.crafter.structure.minecraft.protocol.packet

import com.crafter.structure.minecraft.protocol.writeString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID

class LoginStartPacket(
    private val username: String,
    private val hasUUID: Boolean,
    private val uuid: UUID = UUID.randomUUID(),
    override val packetId: Int = 0x00
) : Packet {
    override fun toByteArray(): ByteArray {
        val packet = ByteArrayOutputStream()
        val stream = DataOutputStream(packet)

        stream.writeByte(packetId)
        stream.writeString(username)
        stream.writeBoolean(hasUUID)
        stream.writeLong(uuid.mostSignificantBits)
        stream.writeLong(uuid.leastSignificantBits)

        return packet.toByteArray()
    }
}