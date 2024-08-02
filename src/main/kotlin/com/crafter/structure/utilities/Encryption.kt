package com.crafter.structure.utilities

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/* This class is implemented as-is because it is specifically designed for securing a minimal RCON password.
* If this were for user authentication or a more complex system, we would have implemented it differently and more robustly.
* However, since RCON requires the original password to function properly, and considering the limitations and lack of alternative options,
* we have used this straightforward approach. Thus, the class is kept in its current form due to these constraints.
*/
object Encryption {
    /**
     * Explanation of how everything works:
     *
     * 1. We store a system environment variable named CRAFTER_SECRET_KEY and retrieve its value.
     *
     * 2. Next, we attempt to encrypt or decrypt a password.
     *
     * 3. If we encrypt a password with a specific key, it will be encrypted with that key and cannot be decrypted with a different key.
     *
     *    For example, if we encrypt the string "aboba" with the key "ANTON", we obtain a Base64-encoded value.
     *    If we then attempt to decrypt this Base64-encoded value with a different key, such as "ANTON2",
     *    we will encounter a padding error.
     */
    private const val ALGORITHM = "AES"
    private val staticIv = IvParameterSpec((0..15).map { it.toByte() }.toByteArray())

    private val cipher = Cipher.getInstance("$ALGORITHM/CBC/PKCS5Padding")
    private val secretKey = SecretKeySpec(System.getenv("CRAFTER_SECRET_KEY").toByteArray(), ALGORITHM)

    @OptIn(ExperimentalEncodingApi::class)
    fun encryptPassword(password: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, staticIv)
        val cipherText = cipher.doFinal(password.toByteArray())

        return Base64.encode(cipherText)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decryptPassword(password: String): String {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, staticIv)
        val cipherText = cipher.doFinal(Base64.decode(password))

        return String(cipherText)
    }
}