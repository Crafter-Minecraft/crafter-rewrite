package com.crafter

import java.util.*

class Property(private val value: String, private val filename: String = "application") {
    private val properties by lazy { loadProperties() }
    private fun loadProperties(): Properties {
        val properties = Properties()
        val propertiesStream = ClassLoader.getSystemClassLoader().getResourceAsStream("$filename.properties")
            ?: throw IllegalArgumentException("Properties file not found")

        properties.load(propertiesStream)
        return properties
    }

    fun getString(): String = properties.getProperty(value)
}