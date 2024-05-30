package com.example

import com.fazecast.jSerialComm.SerialPort
import java.util.*
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
    }

    private fun handleNewSerialState(newState: State) {
        state.setFromOtherState(newState)
    }

    private fun update() {
        val now = System.currentTimeMillis()

        //println("Update $now")
        serialManager.writeOutputs(state)

        if (state.changed) {
            db.insertNewState(state)
            state.changed = false
        }
    }

    private fun captureImage() {

        //println("found webcam $webcam")


        /*   webcam.image?.let {
               val file = File("webcam.png")

               if (file.exists()) {
                   val copy = File("webcam_${System.currentTimeMillis()}.png")
                   file.copyTo(copy)
               }
               ImageIO.write(it, "PNG", file)
           }*/


        //if (webcams.isEmpty()) println("Found no webcam")
    }
}