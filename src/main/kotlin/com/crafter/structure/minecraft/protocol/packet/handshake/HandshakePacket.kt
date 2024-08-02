package com.crafter.structure.minecraft.protocol.packet.handshake

import com.crafter.structure.minecraft.protocol.packet.Packet
import com.crafter.structure.minecraft.protocol.writeString
import com.crafter.structure.minecraft.protocol.writeVarInt
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class HandshakePacket(
    private val address: String,
    private val port: Int,
    private val protocolVersion: Int,
    private val state: HandshakeState,
    override val packetId: Int = 0x00
) : Packet {
    override fun toByteArray(): ByteArray {
        val packet = ByteArrayOutputStream()
        val stream = DataOutputStream(packet)

        stream.writeByte(packetId)
        stream.writeVarInt(protocolVersion)
        stream.writeString(address)
        stream.writeShort(port)
        stream.writeVarInt(state.type)

        return packet.toByteArray()
    }
}