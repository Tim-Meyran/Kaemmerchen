package com.example

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent


class SerialManager {

    var mistifier: Int = 0
    var light = 0
    var fan1 = 0
    var fan2 = 0
    var pump1 = 0
    var pump2 = 0

    var port: SerialPort? = null
    
    fun open(port: SerialPort) {
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
                println("Received data of size: " + newData.size)
                for (i in newData.indices)
                    print(Char(newData[i].toUShort()))
                println("\n")
            }
        })
    }

    fun writeOutputs() {
        var byte: Int = 0

        byte = byte or (mistifier shl 0)
        byte = byte or (light shl 1)
        byte = byte or (fan1 shl 2)
        byte = byte or (fan2 shl 3)
        byte = byte or (pump1 shl 4)
        byte = byte or (pump2 shl 5)

        if (port != null && port!!.isOpen) {
            port!!.writeBytes(byteArrayOf(byte.toByte()), 1)
        }

    }
}