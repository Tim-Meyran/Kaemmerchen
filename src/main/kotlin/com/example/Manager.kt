package com.example

import com.fazecast.jSerialComm.SerialPort
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

class Manager(private val state: State) {

    private val timer = Timer()
    private val db = DiskDatabase()
    private val serialManager = SerialManager(this::handleNewSerialState)

    init {
        state.mistifier = 0
        state.light = 0
        state.fan1 = 0
        state.fan2 = 0
        state.pump1 = 0
        state.pump2 = 0

        val comportName: String? = PropertiesReader.getProperty("COM_PORT")

        for (port in SerialPort.getCommPorts()) {
            println("Found Com Port <${port.systemPortName}> - <${port.descriptivePortName}>")
            if (port.systemPortName.equals(comportName)) {
                serialManager.open(port)
                break
            }
        }

        timer.scheduleAtFixedRate(1000, 500) {
            update()
        }

        timer.scheduleAtFixedRate(1000, 60_000) {
            captureImage()
        }

        timer.scheduleAtFixedRate(1000, 60_000) {
            db.insertNewState(state)
        }
    }

    private fun handleNewSerialState(newState: State) {
        state.setFromOtherState(newState)
    }

    private fun update() {
        val now = System.currentTimeMillis()

        //println("Update $now")
        serialManager.writeOutputs(state)

        if (state.changed) {
            state.changed = false
        }
    }

    private fun captureImage() {
        if (state.light == 0L)
            return

        val captureCmd: String? = PropertiesReader.getProperty("CAPTURE_CMD")

        captureCmd?.let {
            println("Executing $it")
            val p: Process = Runtime.getRuntime().exec(it)
            p.waitFor(15, TimeUnit.SECONDS)

            val imageFile = File("webcam.png")
            if (imageFile.exists()) {
                File("timelapse").mkdirs()
                imageFile.copyTo(File("timelapse/image_${System.currentTimeMillis()}.png"))
            }
        }
    }
}