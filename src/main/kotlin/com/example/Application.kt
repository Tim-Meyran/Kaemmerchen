package com.example

import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File


fun main(args: Array<String>) {
    val logConfigFile = File("logback.xml")
    if (logConfigFile.exists()) {
        System.setProperty("logback.configurationFile", logConfigFile.absolutePath);
        println("set config file to ${logConfigFile.absolutePath}")
    }
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module() {
    configureRouting()
    install(Thymeleaf) {
        println(if (developmentMode) "devMode" else "Release Mode")
        setTemplateResolver((if (developmentMode) {
            FileTemplateResolver().apply {
                cacheManager = null
                prefix = "src/main/resources/templates/"
            }
        } else {
            ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
            }
        }).apply {
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
}
