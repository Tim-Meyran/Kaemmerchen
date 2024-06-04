package com.example

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortMessageListener


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

        port.addDataListener(object : SerialPortMessageListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED
            }

            override fun getMessageDelimiter(): ByteArray {
                return "\r\n".toByteArray(Charsets.UTF_8)
            }

            override fun delimiterIndicatesEndOfMessage(): Boolean {
                return true
            }

            override fun serialEvent(event: SerialPortEvent) {
                val delimitedMessage = event.receivedData
                if (delimitedMessage.size > 24) {
                    //println("Received: ${delimitedMessage.toString(Charsets.UTF_8)}")
                    receiveData(delimitedMessage)
                }
            }
        })

    }

    private fun receiveData(readBuffer: ByteArray) {
        try {
            val stringData = readBuffer.toString(Charsets.UTF_8)
            for (line in stringData.split("\r\n").filter { l -> l.isNotEmpty() }) {
                //println("Received line : $line")
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
                newState.humiditySoil1 = (100.0 * (1.0 - (values[8].toDouble() / 1024.0))).toLong()
                newState.humiditySoil2 = (100.0 * (1.0 - (values[9].toDouble() / 1024.0))).toLong()
                newState.waterLevel = values[10].toLong()

                //println("Parsed $newState")
                callback(newState)
            }
        } catch (ex: Exception) {
            //ex.printStackTrace()
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

        //println("Sending ${byte.toByte()}")

        if (port != null && port!!.isOpen) {
            port!!.writeBytes(byteArrayOf(byte.toByte()), 1)
        }
    }

}