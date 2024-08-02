package com.crafter.structure.minecraft.protocol

import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.experimental.and

object VarInt {
    const val SEGMENT_BITS = 0x7F.toByte()
    const val CONTINUE_BIT = 0x80.toByte()
}

fun DataOutputStream.writeVarInt(value: Int) {
    var intValue = value
    while (true) {
        if ((intValue and 0x7F.inv()) == 0) {
            writeByte(intValue)
            return
        }
        writeByte(intValue and 0x7F or 0x80)
        intValue = intValue ushr 7
    }
}

fun DataOutputStream.writeString(value: String) {
    val bytes = value.toByteArray()
    writeVarInt(bytes.size)
    write(bytes)
}

fun readVarInt(inputStream: DataInputStream): Int {
    var numRead = 0
    var result = 0
    var read: Byte

    do {
        read = inputStream.readByte()
        val value = (read.toInt() and 0x7F)
        result = result or (value shl (7 * numRead))

        numRead++
        if (numRead > 5) throw RuntimeException("VarInt is too big")
    } while ((read.toInt() and 0x80) != 0)

    return result
}
