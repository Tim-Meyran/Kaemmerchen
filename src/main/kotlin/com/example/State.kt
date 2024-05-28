package com.example

data class State(
    var mistifier: Long? = null,
    var light: Long? = null,
    var fan1: Long? = null,
    var fan2: Long? = null,
    var pump1: Long? = null,
    var pump2: Long? = null,
    var temperature: Long = 0,
    var humidity: Long = 0,
    var humiditySoil1: Long = 0,
    var humiditySoil2: Long = 0,
    var waterLevel: Long = 0,

    var changed: Boolean = false
) {

    fun setFromOtherState(other: State) {
        other.mistifier?.let { this.mistifier = it }
        other.light?.let { this.light = it }
        other.fan1?.let { this.fan1 = it }
        other.fan2?.let { this.fan2 = it }
        other.pump1?.let { this.pump1 = it }
        other.pump2?.let { this.pump2 = it }

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