@file:Suppress("unused")

package com.crafter.protocol

import com.crafter.annotations.UnstableApi
import com.crafter.protocol.packet.clientbound.LoginStartPacket
import com.crafter.protocol.packet.Packet
import com.crafter.protocol.packet.clientbound.StatusRequestPacket
import com.crafter.protocol.packet.clientbound.handshake.HandshakePacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.crafter.protocol.packet.clientbound.handshake.HandshakeState
import java.io.*
import java.net.Socket
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

@UnstableApi
class MinecraftProtocol(private val address: String, private val port: Int) : Closeable {
    private var currentThreshold: Int = -1

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

    suspend fun sendPacket(packet: Packet) = withContext(Dispatchers.IO) {
        val out = ByteArrayOutputStream()
        val packetStream = DataOutputStream(out)
        val packetData = packet.toByteArray()

        packetStream.writeVarInt(packetData.size)
        packetStream.write(packetData)
        outputStream!!.write(out.toByteArray())
        outputStream!!.flush()
    }

    private suspend fun readPacket(): String = withContext(Dispatchers.IO) {
        val packetLength = readVarInt(inputStream!!)
        val packetId = readVarInt(inputStream!!)
        val realPacketLength = packetLength - calculateVarIntSize(packetId)

        if (currentThreshold > 0) {
            val dataLength = readVarInt(inputStream!!)

            return@withContext if (dataLength == 0) {
                val data = ByteArray(packetLength - 1)
                inputStream!!.readFully(data)
                String(data)
            } else {
                val compressedData = ByteArray(realPacketLength)
                inputStream!!.readFully(compressedData)
                val uncompressedData = decompressPacket(compressedData)
                String(uncompressedData)
            }
        } else {
            val prefixedLength = readVarInt(inputStream!!)
            val data = ByteArray(prefixedLength)
            inputStream!!.readFully(data)
            return@withContext String(data)
        }
    }

    private suspend fun sendStatusRequest(packet: Packet) = withContext(Dispatchers.IO) {
        val request = StatusRequestPacket(packet.packetId)
        sendPacket(request)

        val responseData = readPacket()

        return@withContext responseData
    }

    suspend fun sendHandshake(protocolVersion: ProtocolVersion, state: HandshakeState): String = withContext(Dispatchers.IO) {
        val handshakePacket = HandshakePacket(address, port, protocolVersion.number, state)
        sendPacket(handshakePacket)

        val responseData = if (state == HandshakeState.State) { sendStatusRequest(handshakePacket) } else ""

        return@withContext responseData
    }

    /**
     Send it separately of other connections.
     */
    suspend fun sendLegacyPing() = withContext(Dispatchers.IO) {
        val legacyPingPacket = StatusRequestPacket.Legacy()
        val inputStreamReader = InputStreamReader(inputStream!!, Charsets.UTF_16BE)

        // TODO: legacySendPacket function.
        outputStream!!.write(legacyPingPacket.toByteArray())
        outputStream!!.flush()

        inputStream!!.read() // Packet ID
        val dataLength = inputStreamReader.read()

        val outputData = CharArray(dataLength)
        inputStreamReader.read(outputData)

        return@withContext String(outputData)
    }

    suspend fun sendLoginStart(protocolVersion: ProtocolVersion, username: String, hasUUID: Boolean?, uuid: UUID) = withContext(Dispatchers.IO) {
        val loginStart = LoginStartPacket(protocolVersion.number, username, hasUUID, uuid)
        sendPacket(loginStart)

        readVarInt(inputStream!!) // Length
        val receivedPacketID = readVarInt(inputStream!!) // Packet ID

        // 0x03 is Set Compression Packet.
        // See https://wiki.vg/Protocol#Set_Compression
        if (receivedPacketID == 0x03) {
            val threshold = readVarInt(inputStream!!)
            currentThreshold = threshold
        }

        return@withContext readPacket()
    }

    override fun close() {
        socket?.close()
        inputStream?.close()
        outputStream?.close()
    }

    private fun compressPacket(data: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        val stream = DeflaterOutputStream(output)

        stream.write(data)
        stream.close()

        return output.toByteArray()
    }

    private fun decompressPacket(data: ByteArray): ByteArray {
        val input = ByteArrayInputStream(data)
        val inflater = InflaterInputStream(input)

        return inflater.readBytes()
    }

    init {
        runBlocking {
            withContext(Dispatchers.IO) { connect() }
        }
    }
}