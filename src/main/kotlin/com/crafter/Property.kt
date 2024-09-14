package com.crafter

import java.io.FileNotFoundException
import java.util.*
import kotlin.reflect.KProperty

class Property(private val value: String, private val filename: String = "application", val systemProperty: Boolean = false) {
    private val properties: Properties = loadProperties()

    @Throws(IllegalArgumentException::class)
    private fun loadProperties(): Properties {
        val properties = Properties()
        val propertiesStream = ClassLoader.getSystemClassLoader().getResourceAsStream("$filename.properties")
            ?: throw IllegalArgumentException("Properties file not found")

        properties.load(propertiesStream)
        return properties
    }

    @Throws(FileNotFoundException::class, NullPointerException::class)
    fun getString(): String = if (systemProperty) {
        System.getProperty(value)
    } else {
        properties.getProperty(value)
    }

    operator fun getValue(ref: Any?, property: KProperty<*>): String = getString()
}