package com.crafter.structure.utilities

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


object ImageUtils {
    @OptIn(ExperimentalEncodingApi::class)
    @Throws(IOException::class)
    fun decodeBase64ToFile(base64Image: String, filePath: String): File {
        val base64Data = base64Image.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val imageBytes: ByteArray = Base64.decode(base64Data)

        val file = File(filePath)
        FileOutputStream(file).use { fos ->
            fos.write(imageBytes)
        }
        return file
    }
}