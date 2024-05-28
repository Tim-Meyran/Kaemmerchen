package com.example

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent


class SerialManager {

    private var port: SerialPort? = null

    fun open(port: SerialPort, callback: (arr: State) -> Unit) {
        if (this.port != null && this.port!!.isOpen) {
            return
        }

        this.port = port
        port.setBaudRate(57600)
        if (!port.isOpen) port.openPort()

        port.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED
            }

            override fun serialEvent(event: SerialPortEvent) {
                val newData = event.receivedData

                val stringData = newData.toString(Charsets.UTF_8)
                for (line in stringData.split("\n\r")) {
                    println("Received line : $line")
                    val values = line.split(";")
                    if (values.size != 11) continue

                    val newState = State()
                    newState.mistifier = values[0].toLong()
                    newState.light = values[1].toLong()
                    newState.fan1 = values[2].toLong()
                    newState.fan2 = values[3].toLong()
                    newState.pump1 = values[4].toLong()
                    newState.pump2 = values[5].toLong()

                    newState.temperature = values[6].toLong()
                    newState.humidity = values[7].toLong()
                    newState.humiditySoil1 = values[8].toLong()
                    newState.humiditySoil2 = values[9].toLong()
                    newState.waterLevel = values[10].toLong()
                    callback(newState)
                }
            }
        })
    }

    fun writeOutputs(state: State) {
        var byte = 0
        byte = byte or (state.mistifier.toInt() shl 0)
        byte = byte or (state.light.toInt() shl 1)
        byte = byte or (state.fan1.toInt() shl 2)
        byte = byte or (state.fan2.toInt() shl 3)
        byte = byte or (state.pump1.toInt() shl 4)
        byte = byte or (state.pump2.toInt() shl 5)

        if (port != null && port!!.isOpen) {
            port!!.writeBytes(byteArrayOf(byte.toByte()), 1)
        }
    }
}