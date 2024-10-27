package com.crafter.discord.t9n

import com.crafter.discord.Initializable
import net.dv8tion.jda.api.interactions.DiscordLocale
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

/** t9n singleton for translating. **/
object T9nProtocol : Initializable {
    private const val LOCALIZATION_URL: String =
        "https://raw.githubusercontent.com/Crafter-Minecraft/crafter-localization/refs/heads/main/%s.properties"
    val AVAILABLE_LOCALES = listOf("ru", "en-US")

    private val httpClient: OkHttpClient = OkHttpClient()
    private val translations: MutableMap<String, Properties> = mutableMapOf()

    override fun initialize() = loadTranslations()

    private fun fetchLocalizationText(locale: String): InputStream? {
        val request = Request.Builder()
            .url(LOCALIZATION_URL.format(locale.replace("-", "_")))
            .get()
            .build()
        val response = httpClient
            .newCall(request)
            .execute()

        return response.use {
            if (it.isSuccessful) {
                it.body?.bytes()?.let { bytes -> ByteArrayInputStream(bytes) }
            } else {
                null
            }
        }
    }

    private fun loadTranslations() = AVAILABLE_LOCALES.forEach { locale ->
        val properties = Properties()

        fetchLocalizationText(locale)?.let { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                properties.load(reader)
            }
        }

        translations[locale] = properties
    }

    /**
     * Return translated text
     * @param key The key for the translation
     * @param locale Locale for the translation, e.g., "ru_RU"
     * @return Translated text or default value if key is not found
     */
    fun text(key: String, locale: String): String {
        val properties = translations[locale]
        return properties?.getProperty(key) ?: key
    }
}

/** An alias for text from T9nProtocol **/
fun text(key: String, locale: DiscordLocale) =
    T9nProtocol.text(key, locale.locale)