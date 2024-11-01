package com.crafter.protocol.packet.clientbound

import com.crafter.protocol.ProtocolVersion
import com.crafter.protocol.packet.Packet
import com.crafter.protocol.writeString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID

class LoginStartPacket(
    private val protocolVersion: Int,
    private val username: String,
    private val hasUUID: Boolean? = null,
    private val uuid: UUID = UUID.randomUUID(),
    override val packetId: Int = 0x00
) : Packet {
    override fun toByteArray(): ByteArray {
        val packet = ByteArrayOutputStream()
        val stream = DataOutputStream(packet)

        stream.writeByte(packetId)
        stream.writeString(username)
        if (ProtocolVersion.V1_19 lowerOrEqualThan protocolVersion && hasUUID != null) { // I don't know on what version they added it :sob:
            stream.writeBoolean(hasUUID)
        }
        stream.writeLong(uuid.mostSignificantBits)
        stream.writeLong(uuid.leastSignificantBits)

        return packet.toByteArray()
    }
}