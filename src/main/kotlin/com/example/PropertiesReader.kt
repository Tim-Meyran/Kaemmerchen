package com.example

import java.io.File
import java.util.*

private const val CONFIG = "config.properties"

object PropertiesReader {
    private val properties = Properties()
    private val file = File(CONFIG)

    fun getProperty(key: String): String? {
        properties.load(file.inputStream())
        return properties.getProperty(key)
    }
}