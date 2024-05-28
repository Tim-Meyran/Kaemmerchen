package com.example

import com.fazecast.jSerialComm.SerialPort
import com.github.sarxos.webcam.Webcam
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.scheduleAtFixedRate

class Manager(val state: State) {

    val timer = Timer()

    val db = DiskDatabase()

    val serialManager = SerialManager()

    init {
        for (port in SerialPort.getCommPorts()) {
            serialManager.open(port, this::handleNewSerialState)
            break
        }

        timer.scheduleAtFixedRate(1000, 500) {
            update()
        }

        timer.scheduleAtFixedRate(1000, 60_000) {
            captureImage()
        }
    }

    private fun handleNewSerialState(newState: State) {
        state.setFromOtherState(newState)
    }

    private fun update() {
        val now = System.currentTimeMillis()

        println("Update $now")
        serialManager.writeOutputs(state)

        if (state.changed) {
            db.insertNewState(state)
            state.changed = false
        }
    }

    private fun captureImage() {
        val webcams = Webcam.getWebcams()
        for (webcam in webcams) {
            println("found webcam $webcam")

            if (!webcam.isOpen)
                webcam.open()

            webcam.image?.let {
                val file = File("webcam.png")

                if (file.exists()) {
                    val copy = File("webcam_${System.currentTimeMillis()}.png")
                    file.copyTo(copy)
                }

                ImageIO.write(it, "PNG", file)
            }
        }

        if (webcams.isEmpty()) println("Found no webcam")
    }
}