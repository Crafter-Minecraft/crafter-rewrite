package com.crafter.structure.minecraft.rcon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.TimeoutException

class RconController(private val ip: String, private val port: Int, private val password: String) {
    private val colorCodes = listOf(
        "§0", "§1", "§2",
        "§3", "§4", "§5",
        "§6", "§7", "§8",
        "§9", "§a", "§b",
        "§c", "§d", "§e",
        "§f", "§g", "§o",
        "§l"
    )

    private val availableCharsRegex = """[a-zA-Zа-яА-Я\s.,!?;:'"-()«»—_`{}\[\]<>/\\|]""".toRegex()
    // MaGiC
    private val authResponseID = 167772160

    private var requestId: Int = 0
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null

    private suspend fun authenticate(): Boolean {
        return try {
            socket = withContext(Dispatchers.IO) { Socket(ip, port).apply {
                soTimeout = 5000
            } }

            outputStream = DataOutputStream(withContext(Dispatchers.IO) { socket!!.getOutputStream() })
            inputStream = DataInputStream(withContext(Dispatchers.IO) { socket!!.getInputStream() })

            val response = sendPacket(PacketType.SERVERDATA_AUTH, password, requestId)

            response.id == authResponseID && response.code == 0
        } catch (e: ConnectException) {
            false
        } catch (e: TimeoutException) {
            false
        }
    }

    private suspend fun sendPacket(type: PacketType, command: String, id: Int): RconResponse {
        val packet = RconPacket(id, type, command)

        withContext(Dispatchers.IO) {
            outputStream!!.write(packet.toByteArray())
            outputStream!!.flush()
        }

        val responseId = withContext(Dispatchers.IO) { inputStream!!.readInt() }
        val responseCode = withContext(Dispatchers.IO) { inputStream!!.readInt() }
        var responseMessage = readString()

        colorCodes.forEach {
            responseMessage = responseMessage.replace(it, "")
        }

        return RconResponse(responseId, responseCode, responseMessage.filter { it.toString().matches(availableCharsRegex) })
    }
    private suspend fun readString(): String {
        val buffer = ByteArray(1024 * 16)
        withContext(Dispatchers.IO) {
            inputStream!!.read(buffer)
        }

        return String(buffer)
    }

    private suspend fun sendCommand(command: String): List<RconResponse> {
        var response: RconResponse?
        val responses = mutableListOf<RconResponse>()

        // It's needed for multi-packet response.
        do {
            response = sendPacket(PacketType.SERVERDATA_EXECCOMMAND, command, requestId++)

            if (response.message.isNotEmpty()) {
                responses.add(response)
            }
        } while (response?.code != 0)

        return responses
    }

    suspend fun send(command: String): List<RconResponse> {
        authenticate()
        val response = sendCommand(command)
        close()

        return response
    }

    @Throws(IOException::class)
    fun close() {
        socket?.close()
        inputStream?.close()
        outputStream?.close()
    }
}