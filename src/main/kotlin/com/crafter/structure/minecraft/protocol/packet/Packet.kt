package com.crafter.structure.minecraft.protocol.packet

import java.io.File
import kotlin.reflect.full.*

interface Packet {
    val packetId: Int
    fun toByteArray(): ByteArray
}

// Impossible?
/*
private const val prefix = "src.main.kotlin.com.crafter"
private const val packageName = "$prefix.structure.minecraft.protocol.packet"
private val packagePath = packageName.replace(".", "/")

fun packetById(packetId: Int) {
    val directory = File(packagePath)

    if (!directory.exists() || !directory.isDirectory) return

    directory.walkTopDown().forEach { file ->
        if (file.isDirectory || !file.name.endsWith(".kt") || file.name.lowercase() == "packet.kt") return@forEach

        val name = file.parentFile.relativeTo(directory)
            .path
            .replace(File.separatorChar, '.')
            .let { "$packageName.$it.${file.nameWithoutExtension}" }
            .replace(prefix, "com.crafter")

        val clazz = Class.forName(name)

        println(clazz.kotlin.objectInstance)
    }
} */