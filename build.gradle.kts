val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    id("app.cash.sqldelight") version "2.0.2"
}

group = "com.example"
version = "v0.0.3"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = true //project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-thymeleaf")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("com.github.sarxos:webcam-capture:0.3.12")

    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")



    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.example")
        }
    }
}