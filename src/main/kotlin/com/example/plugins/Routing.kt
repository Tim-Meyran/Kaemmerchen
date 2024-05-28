package com.example.plugins

import com.example.CronJob
import com.example.DiskDatabase
import com.fazecast.jSerialComm.SerialPort
import com.github.sarxos.webcam.Webcam
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kotlinx.html.*
import java.io.File
import javax.imageio.ImageIO


fun Application.configureRouting() {

    val db = DiskDatabase()

    val cronjob = CronJob()

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf()))
        }

        get("/inputs") {
            val m = mapOf("" to "")

            call.respond(ThymeleafContent("inputs", mapOf("data" to db.getDataPointsWithName())))
        }

        get("/outputs") {
            call.respond(ThymeleafContent("outputs", mapOf("data" to db.getDatapoints())))
        }

        post("/set_output") {
            val formParameters = call.receiveParameters()
            println(formParameters)
            //val username = formParameters["username"].toString()
            //call.respondText("The '$username' account is created")
        }

        get("/take-picture") {
            val webcams = Webcam.getWebcams()
            for (webcam in webcams) {
                println("found webcam $webcam")

                webcam.open()
                webcam.image?.let {
                    val file = File("image.png")
                    ImageIO.write(it, "PNG", file)

                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "image.png")
                            .toString()
                    )
                    call.respondFile(file)
                    return@get
                }
            }

            if (webcams.isEmpty()) println("Found no webcams")
        }

        get("/serial-ports") {
            val ports = SerialPort.getCommPorts()
            println("getting serial ports")
            for (port in ports) {
                println(port.systemPortName)
            }
        }

        get("/players") {
            call.respond(ThymeleafContent("players", mapOf("players" to db.getDatapoints())))
        }

        get("/login") {
            call.respondHtml {
                body {
                    form(
                        action = "/login",
                        encType = FormEncType.applicationXWwwFormUrlEncoded,
                        method = FormMethod.post
                    ) {
                        p {
                            +"Username:"
                            textInput(name = "username")
                        }
                        p {
                            +"Password:"
                            passwordInput(name = "password")
                        }
                        p {
                            submitInput() { value = "Login" }
                        }
                    }
                }
            }
        }

        staticResources("/static", "static")

    }
}

