package com.crafter.structure.minecraft.rcon

class RconPacket(val id: Int, val packetType: PacketType, val body: String) {
    fun toByteArray(): ByteArray {
        val bytes = body.toByteArray()
        // Packet length (4), ID (4), Type (4), Body Length, Two Null terminators
        val bodyLength = bytes.size + 4 + 4 + 4 + 2 + 2

        return ByteArray(bodyLength).apply {
            // https://developer.valvesoftware.com/wiki/Source_RCON_Protocol#Packet_Size
            // "...so the value of this field is always 4 less than the packet's actual length."
            // (we're just removing packet length)
            writeInt(0, bodyLength - 4)
            writeInt(4, id)
            writeInt(8, packetType.type)

            // Writing body to the packet
            System.arraycopy(bytes, 0, this, 12, bytes.size)

            // Empty string terminator
            this[bodyLength - 2] = 0
            this[bodyLength - 1] = 0
        }
    }

    /** Extension function for ByteArray that writes 32 little-endian integer **/
    private fun ByteArray.writeInt(offset: Int, value: Int) {
        this[offset] = (value and 255).toByte()
        this[offset + 1] = (value shr 8 and 255).toByte()
        this[offset + 2] = (value shr 16 and 255).toByte()
        this[offset + 3] = (value shr 24 and 255).toByte()
    }
}