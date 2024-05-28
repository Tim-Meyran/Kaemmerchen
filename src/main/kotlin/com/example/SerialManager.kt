package com.example

import com.fazecast.jSerialComm.SerialPort


class SerialManager(val callback: (State) -> Unit) {

    private var port: SerialPort? = null

    fun open(port: SerialPort) {
        if (this.port != null && this.port!!.isOpen) {
            return
        }

        println("Opening com port $port")

        this.port = port
        port.setBaudRate(57600)

        if (!port.isOpen) port.openPort()

        Thread {
            try {
                while (true) {
                    while (port.bytesAvailable() == 0) Thread.sleep(20)

                    val readBuffer = ByteArray(port.bytesAvailable())
                    val numRead: Int = port.readBytes(readBuffer, readBuffer.size)
                    println("Read $numRead bytes.")
                    receiveData(readBuffer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        /*port.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE
            }

            override fun serialEvent(event: SerialPortEvent) {
                if (event.eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return
                val newData = ByteArray(port.bytesAvailable())
                val numRead: Int = port.readBytes(newData, newData.size)
                println("Read $numRead bytes.")


            }
        })*/


    }

    private fun receiveData(readBuffer: ByteArray) {
        try {
            val stringData = readBuffer.toString(Charsets.UTF_8)
            for (line in stringData.split("\r\n").filter { l -> l.isNotEmpty() }) {
                println("Received line : $line")
                val values = line.split(";")
                if (values.size != 11) continue

                val newState = State()/*newState.mistifier = values[0].toLong()
                newState.light = values[1].toLong()
                newState.fan1 = values[2].toLong()
                newState.fan2 = values[3].toLong()
                newState.pump1 = values[4].toLong()
                newState.pump2 = values[5].toLong()*/

                newState.temperature = values[6].toLong()
                newState.humidity = values[7].toLong()
                newState.humiditySoil1 = values[8].toLong()
                newState.humiditySoil2 = values[9].toLong()
                newState.waterLevel = values[10].toLong()

                println("Parsed $newState")
                callback(newState)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun writeOutputs(state: State) {
        var byte = 0
        byte = byte or ((state.mistifier?.toInt() ?: 0) shl 0)
        byte = byte or ((state.light?.toInt() ?: 0) shl 1)
        byte = byte or ((state.fan1?.toInt() ?: 0) shl 2)
        byte = byte or ((state.fan2?.toInt() ?: 0) shl 3)
        byte = byte or ((state.pump1?.toInt() ?: 0) shl 4)
        byte = byte or ((state.pump2?.toInt() ?: 0) shl 5)

        println("Sending ${byte.toByte()}")

        if (port != null && port!!.isOpen) {
            port!!.writeBytes(byteArrayOf(byte.toByte()), 1)
        }
    }
}