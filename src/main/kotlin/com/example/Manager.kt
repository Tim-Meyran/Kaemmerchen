package com.example

import com.fazecast.jSerialComm.SerialPort
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.io.path.getLastModifiedTime

class Manager(private val state: State) {

    private val timer = Timer()
    private val db = DiskDatabase()
    private val serialManager = SerialManager(this::handleNewSerialState)

    val log = LoggerFactory.getLogger("Manager");

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

        timer.scheduleAtFixedRate(1000, 5_000) {
            updateAutomatic()

            //serialManager.receiveLine("0;0;0;0;0;0;27;75;145;341;1\r\n")
        }

        timer.scheduleAtFixedRate(1000, 60 * 60 * 1_000) {
            updatePumps()
        }

        timer.scheduleAtFixedRate(1000, 500) {
            updateOutputs()

            if (state.takeImageNow) {
                state.takeImageNow = false
                captureImage()
            }
        }

        timer.scheduleAtFixedRate(1000, 30 * 60 * 1_000) {
            captureImage()
        }

        /*timer.scheduleAtFixedRate(60_000, 24 * 60 * 60_000) {
            createTimelapse()
        }*/

        timer.scheduleAtFixedRate(1000, 60_000) {
            db.insertNewState(state)
        }
    }

    private fun createTimelapse() {
        val directory = File("timelapse")
        directory.mkdir()

        val imageFileList = File("timelapse/image-list.txt")
        if (!imageFileList.exists()) imageFileList.createNewFile()

        imageFileList.writeText("")

        val files = directory.listFiles()?.filter { it.isFile }
        files?.filter { it.isFile && it.name.endsWith(".png") }?.sortedBy { it.toPath().getLastModifiedTime() }
            ?.forEach { file ->
                imageFileList.appendText("file '${file.name}'\n")
            } ?: println("No files found or directory does not exist.")


        val timelapseCmd: String? = PropertiesReader.getProperty("TIMELAPSE_CMD")
        timelapseCmd?.let {
            val p: Process = Runtime.getRuntime().exec(it)
            p.waitFor(240, TimeUnit.SECONDS)
        }
    }

    private fun updateAutomatic() {
        log.debug("updateAutomatic State <{}>", state)
        if (state.automaticMode == 0L) return

        val currentDateTime = LocalDateTime.now()
        val oldLight = state.light
        state.light = if (currentDateTime.hour >= 6) 1L else 0L
        if (oldLight != state.light)
            log.info("update light - State <{}>", state)
    }

    private fun updatePumps() {
        if (state.automaticMode == 0L) return

        if (state.humiditySoil2 < 65) {
            state.pump2 = 1
            log.info("Start Pump - State <{}>", state)

            Thread.sleep(5000)
            log.info("Stop Pump - State <{}>", state)
            state.pump2 = 0
        }
    }

    private fun handleNewSerialState(newState: State) {
        state.setFromOtherState(newState)
    }

    private fun updateOutputs() {
        val now = System.currentTimeMillis()

        //println("Update $now")
        serialManager.writeOutputs(state)

        if (state.changed) {
            state.changed = false
        }
    }

    private fun captureImage() {
        if (state.light == 0L) return

        log.info("Capture Image")
        val captureCmd: String? = PropertiesReader.getProperty("CAPTURE_CMD")

        captureCmd?.let {
            log.debug("Executing $it")
            val p: Process = Runtime.getRuntime().exec(it)
            p.waitFor(15, TimeUnit.SECONDS)
                .let { successful -> log.info("Capture {}", if (successful) "successful" else " not successful") }

            val imageFile = File("webcam.png")
            if (imageFile.exists()) {
                File("timelapse").mkdirs()
                imageFile.copyTo(File("timelapse/image_${System.currentTimeMillis()}.png"))
            }
        }
    }
}