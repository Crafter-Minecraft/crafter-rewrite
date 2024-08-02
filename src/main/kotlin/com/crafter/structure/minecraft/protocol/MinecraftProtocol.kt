package com.crafter.structure.minecraft.protocol

import com.crafter.structure.minecraft.protocol.packet.LoginAcknowledg
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
import kotlin.math.log

@UnstableApi
class MinecraftProtocol(private val address: String, private val port: Int) : Closeable {
    private val scope = CoroutineScope(Dispatchers.IO)

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
        inputStream!!.read(responseData)

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

        val responseData = request(handshakePacket)

        return@withContext responseData
    }

    suspend fun sendLoginStart(username: String, uuid: UUID) = withContext(Dispatchers.IO) {
        val loginStart = LoginStartPacket(username, uuid)
        sendPacket(loginStart.toByteArray())

        val responseData = request(loginStart)
        return@withContext responseData
    }

    suspend fun sendLoginAcknowledg() {
        val loginAcknowledg = LoginAcknowledg()
        sendPacket(loginAcknowledg.toByteArray())

        request(loginAcknowledg)
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