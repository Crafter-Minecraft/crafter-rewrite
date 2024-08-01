package com.crafter.discord.t9n

import com.crafter.discord.Initializable
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.io.File
import java.util.Properties

/** t9n singleton for translating. **/
object T9nProtocol : Initializable {
    private val translations: MutableMap<String, Properties> = mutableMapOf()

    override fun initialize() = loadTranslations()

    private fun loadTranslations() {
        val path = javaClass.getResource("/translate")?.path ?:
            throw IllegalArgumentException("Translations file not found")

        val directory = File(path)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.filter { it.isFile && it.extension == "properties" }?.forEach { file ->
                val locale = file.nameWithoutExtension.substringAfterLast('_')
                val properties = Properties().apply { load(file.reader(Charsets.UTF_8)) }
                translations[locale] = properties
            }
        }
    }

    /**
     * Return translated text
     * @param key: The key for the translation
     * @param default: Default value if translation is not found
     * @param locale: Locale for the translation, e.g., "ru_RU"
     * @return Translated text or default value if key is not found
     */
    fun text(key: String, default: String, locale: String): String {
        val properties = translations[locale]
        return properties?.getProperty(key, default) ?: default
    }
}

/** An alias for text from T9nProtocol **/
fun text(key: String, default: String, locale: DiscordLocale) =
    T9nProtocol.text(key, default, locale.locale.uppercase())