package com.crafter.structure.minecraft.pinger

class PingPacket(
    private val address: String,
    private val port: Int = 25565
) {
    companion object {
        private const val MAX_IP_LENGTH = 45
    }

    // https://github.com/MagM1go/minecraft-pinger/blob/main/src/utils/mod.rs#L53-L69
    fun toByteArray(): ByteArray {
        val ipLength = address.length
        val buffer = ByteArray(MAX_IP_LENGTH)

        return buffer.apply {
            this[0] = ((ipLength + 6).toByte())
            this[1] = 0x00
            this[2] = 0x2f
            this[3] = ipLength.toByte()

            address.toByteArray().copyInto(this, 4)

            this[4 + ipLength] = (port shr 8).toByte()
            this[4 + ipLength + 1] = port.toByte()
            this[4 + ipLength + 2] = 0x01
            this[4 + ipLength + 3] = 0x01
            this[4 + ipLength + 4] = 0x00
        }
    }
}