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
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.max
import kotlin.math.min


fun Application.configureRouting() {

    val state = State()

    val manager = Manager(state)

    val versionString =
        this::class.java.classLoader.getResource("VERSION")?.readText()?.replace("\${VERSION}", "DEV_MODE")
            ?: "NO_VERSION_FILE"

    println("Version string: $versionString")

    val log = LoggerFactory.getLogger("Router")

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf("git_version" to versionString)))
        }

        get("/plant1") {
            val localDate = LocalDate.parse(state.plantDate1, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val daysSince = ChronoUnit.DAYS.between(localDate, LocalDate.now())
            val data = mapOf(
                "plantId" to "1",
                "name" to state.plantName1,
                "desc" to state.plantDesc1,
                "dateString" to "planted on ${state.plantDate1} - $daysSince days old",
                "targetHumiditySoil" to "${state.targetHumiditySoil1}"
            )
            call.respond(ThymeleafContent("plant", data))
        }

        get("/plant2") {
            val localDate = LocalDate.parse(state.plantDate2, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val daysSince = ChronoUnit.DAYS.between(localDate, LocalDate.now())
            val data = mapOf(
                "plantId" to "2",
                "name" to state.plantName2,
                "desc" to state.plantDesc2,
                "dateString" to "planted on ${state.plantDate2} - $daysSince days old",
                "targetHumiditySoil" to "${state.targetHumiditySoil2}"
            )
            call.respond(ThymeleafContent("plant", data))
        }

        post("/plantConfig") {
            val formParameters = call.receiveParameters()
            println(formParameters)

            val plantId = formParameters["plantId"]
            val name = formParameters["name"] ?: "Plant Name"
            val desc = formParameters["desc"] ?: "Plant desc"
            val date = formParameters["date"] ?: Date.from(Instant.now()).toString()

            if (plantId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            if (plantId == "1") {
                state.plantName1 = name
                state.plantDesc1 = desc
                state.plantDate1 = date
            } else if (plantId == "2") {
                state.plantName2 = name
                state.plantDesc2 = desc
                state.plantDate2 = date
            }

            call.respondRedirect("plant$plantId")
        }
        get("/plantConfig") {
            val plantParam: String? = call.parameters["plant"]
            if (plantParam == null)
                call.respond(HttpStatusCode.BadRequest)

            if (plantParam.equals("1"))
                call.respond(
                    ThymeleafContent(
                        "plantConfig", mapOf(
                            "plantId" to "1",
                            "name" to state.plantName1,
                            "desc" to state.plantDesc1,
                            "date" to state.plantDate1
                        )
                    )
                )
            else if (plantParam.equals("2"))
                call.respond(
                    ThymeleafContent(
                        "plantConfig",
                        mapOf(
                            "plantId" to "2",
                            "name" to state.plantName2,
                            "desc" to state.plantDesc2,
                            "date" to state.plantDate2
                        )
                    )
                )
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
                    "pump1OnDuration" -> state.pump1OnDuration = newStateEntry.value
                    "pump2OnDuration" -> state.pump2OnDuration = newStateEntry.value
                    "lightOnTime" -> state.lightOnTime = min(newStateEntry.value, state.lightOffTime)
                    "lightOffTime" -> state.lightOffTime = max(newStateEntry.value, state.lightOnTime)
                    "pumpInterval" -> state.pumpInterval = newStateEntry.value
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

