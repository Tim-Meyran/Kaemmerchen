package com.example

data class State(
    var mistifier: Long = 0,
    var light: Long = 0,
    var fan1: Long = 0,
    var fan2: Long = 0,
    var pump1: Long = 0,
    var pump2: Long = 0,
    var temperature: Long = 0,
    var humidity: Long = 0,
    var humiditySoil1: Long = 0,
    var humiditySoil2: Long = 0,
    var waterLevel: Long = 0,

    var changed: Boolean = false
) {

    fun setFromOtherState(other: State) {
        this.mistifier = other.mistifier
        this.light = other.light
        this.fan1 = other.fan1
        this.fan2 = other.fan2
        this.pump1 = other.pump1
        this.pump2 = other.pump2
        this.temperature = other.temperature
        this.humidity = other.humidity
        this.humiditySoil1 = other.humiditySoil1
        this.humiditySoil2 = other.humiditySoil2
        this.waterLevel = other.waterLevel
        this.changed = true
    }

    fun getDataPointsWithName(): Map<String, String> {
        return mapOf(
            "Mistifier" to "$mistifier",
            "Light" to "$light",
            "Fan 1" to "$fan1",
            "Fan 2" to "$fan2",
            "Pump 1" to "$pump1",
            "Pump 2" to "$pump2",
            "Temperature" to "$temperature",
            "Humidity" to "$humidity",
            "WaterLevel" to "$waterLevel",
            "Soil Humidity 1" to "$humiditySoil1",
            "Soil Humidity 2" to "$humiditySoil2"
        )
    }
}