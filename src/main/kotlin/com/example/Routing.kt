package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.max


fun Application.configureRouting() {

    val state = State()

    val manager = Manager(state)

    val log = LoggerFactory.getLogger("Router");

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf()))
        }

        get("/webcam") {
            call.respond(ThymeleafContent("webcam", mapOf()))
        }

        get("/inputs") {
            call.respond(ThymeleafContent("inputs", mapOf("data" to state.getDataPointsWithName())))
        }

        get("/outputs") {
            call.respond(ThymeleafContent("outputs", mapOf("data" to state)))
        }

        get("/logging") {
            val logFile = File("kaemmerchen.log")
            var logText = listOf<String>()
            if (logFile.exists()) {
                logText = logFile.readLines().reversed()
            }
            call.respond(ThymeleafContent("logging", mapOf("lines" to logText)))

        }

        post("/take-image") {
            state.takeImageNow = true
            log.info("takeImageNow")
            call.respondRedirect("/")
            //call.response.status(HttpStatusCode.OK)
        }

        post("/set_output") {
            val formParameters = call.receiveParameters()
            println(formParameters)

            val newStateMap = mutableMapOf<String, Long>()
            for (param in formParameters.names()) {
                val name = param.replace("_hidden", "")
                val value: String? = formParameters[param]
                value?.toLong()?.let { v -> newStateMap[name] = max(v, newStateMap.getOrDefault(name, 0)) }
            }

            for (newStateEntry in newStateMap) {
                when (newStateEntry.key) {
                    "mistifier" -> state.mistifier = newStateEntry.value
                    "light" -> state.light = newStateEntry.value
                    "fan1" -> state.fan1 = newStateEntry.value
                    "fan2" -> state.fan2 = newStateEntry.value
                    "pump1" -> state.pump1 = newStateEntry.value
                    "pump2" -> state.pump2 = newStateEntry.value
                    "automaticMode" -> state.automaticMode = newStateEntry.value
                    "targetHumiditySoil1" -> state.targetHumiditySoil1 = newStateEntry.value
                    "targetHumiditySoil2" -> state.targetHumiditySoil2 = newStateEntry.value
                }
                state.changed = true
            }
            log.info("set_output <{}>", state)

            call.respondRedirect("/")
        }

        get("/live-image") {
            val file = File("webcam.png")
            if (!file.exists()) {
                call.response.status(HttpStatusCode(418, "I'm a tea pot"))
                return@get
            }

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "webcam.png")
                    .toString()
            )
            call.respondFile(file)
        }

        get("/state") {
            val json = Json.encodeToString(state)
            call.respondText(json)
        }

        staticResources("/static", "static")
    }
}

