package com.example

import java.io.File
import java.util.*

private const val CONFIG = "config.properties"

object PropertiesReader {
    private val properties = Properties()

    init {
        val file = File(CONFIG)
        properties.load(file.inputStream())
    }

    fun getProperty(key: String): String? = properties.getProperty(key)
}