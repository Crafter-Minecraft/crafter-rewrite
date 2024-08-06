@file:Suppress("unused")

package com.crafter.structure.minecraft.protocol

import com.crafter.structure.minecraft.protocol.packet.LoginAcknowledge
import com.crafter.structure.minecraft.protocol.packet.LoginStartPacket
import com.crafter.structure.minecraft.protocol.packet.Packet
import com.crafter.structure.minecraft.protocol.packet.RequestPacket
import com.crafter.structure.minecraft.protocol.packet.handshake.HandshakePacket
import com.crafter.structure.minecraft.protocol.packet.handshake.HandshakeState
import com.crafter.structure.utilities.UnstableApi
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.UUID

@UnstableApi
class MinecraftProtocol(private val address: String, private val port: Int) : Closeable {
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null

    private suspend fun connect() {
        return withContext(Dispatchers.IO) {
            socket = Socket(address, port).apply {
                soTimeout = 30_000
            }

            outputStream = DataOutputStream(socket!!.getOutputStream())
            inputStream = DataInputStream(socket!!.getInputStream())
        }
    }

    private suspend fun sendPacket(packetData: ByteArray) = withContext(Dispatchers.IO) {
        val packet = ByteArrayOutputStream()
        val packetStream = DataOutputStream(packet)

        packetStream.writeVarInt(packetData.size)
        packetStream.write(packetData)
        outputStream!!.write(packet.toByteArray())
        outputStream!!.flush()
    }

    private suspend fun readPacket(): String = withContext(Dispatchers.IO) {
        readVarInt(inputStream!!) // Length
        readVarInt(inputStream!!) // Packet ID
        val dataLength = readVarInt(inputStream!!)
        val responseData = ByteArray(dataLength)

        inputStream!!.readFully(responseData)

        return@withContext String(responseData)
    }

    private suspend fun request(packet: Packet) = withContext(Dispatchers.IO) {
        val request = RequestPacket(packet.packetId)
        sendPacket(request.toByteArray())

        val responseData = readPacket()

        return@withContext responseData
    }

    suspend fun sendHandshake(protocolVersion: Int, state: HandshakeState): String = withContext(Dispatchers.IO) {
        val handshakePacket = HandshakePacket(address, port, protocolVersion, state)
        sendPacket(handshakePacket.toByteArray())

        val responseData = if (state == HandshakeState.State) {
            request(handshakePacket)
        } else ""

        return@withContext responseData
    }

    suspend fun sendLoginStart(username: String, hasUUID: Boolean, uuid: UUID) = withContext(Dispatchers.IO) {
        val loginStart = LoginStartPacket(username, hasUUID, uuid)
        sendPacket(loginStart.toByteArray())

        return@withContext readPacket()
    }

    suspend fun sendLoginAcknowledge() {
        val loginAcknowledge = LoginAcknowledge()
        sendPacket(loginAcknowledge.toByteArray())
    }

    override fun close() {
        socket?.close()
        inputStream?.close()
        outputStream?.close()
    }

    init {
        runBlocking {
            withContext(Dispatchers.IO) { connect() }
        }
    }
}