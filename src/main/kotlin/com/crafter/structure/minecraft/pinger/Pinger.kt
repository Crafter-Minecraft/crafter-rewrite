package com.crafter.structure.minecraft.pinger

import com.crafter.structure.utilities.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

@UnstableApi
class Pinger {
    private var socket: Socket? = null
    var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null

    suspend fun connect(address: String, port: Int): Boolean {
        return try {
            socket = withContext(Dispatchers.IO) { Socket(address, port).apply {
                soTimeout = 30_000
            } }

            outputStream = DataOutputStream(withContext(Dispatchers.IO) { socket!!.getOutputStream() })
            inputStream = DataInputStream(withContext(Dispatchers.IO) { socket!!.getInputStream() })
            true
        } catch (e: TimeoutException) {
            false
        }
    }

    suspend fun readPacket(inputStream: DataInputStream): ByteArray = withContext(Dispatchers.IO) {
        val buffer = mutableListOf<Byte>()
        val byte = ByteArray(1)
        var trailingZeros = false

        while (true) {
            try {
                if (inputStream.read(byte) == 1) {
                    if (byte[0] == 0.toByte()) {
                        if (trailingZeros) {
                            break
                        } else {
                            trailingZeros = true
                        }
                    } else {
                        trailingZeros = false
                        buffer.add(byte[0])
                    }
                }
            } catch (e: SocketTimeoutException) {
                break
            }
        }

        return@withContext buffer.toByteArray()
    }

    suspend fun getServerInfo(ip: String, port: Int): JSONObject {
        connect(ip, port)
        val outputStream = DataOutputStream(withContext(Dispatchers.IO) { socket!!.getOutputStream() })
        val inputStream = DataInputStream(withContext(Dispatchers.IO) { socket!!.getInputStream() })

        val pingPacket = PingPacket(ip, port).toByteArray()
        withContext(Dispatchers.IO) {
            outputStream.write(pingPacket)
            outputStream.flush()
        }

        val response = readPacket(inputStream)
        val responseStr = String(response.copyOfRange(4, response.size), Charsets.UTF_8)
        val responseJson = JSONObject(responseStr)

        return responseJson
    }
}