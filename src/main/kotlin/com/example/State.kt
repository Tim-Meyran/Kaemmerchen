package com.example

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class State(
    var mistifier: Long? = null,
    var light: Long? = null,
    var fan1: Long? = null,
    var fan2: Long? = null,
    var pump1: Long? = null,
    var pump2: Long? = null,

    @Required var temperature: Long = 0,
    @Required var humidity: Long = 0,
    @Required var humiditySoil1: Long = 0,
    @Required var humiditySoil2: Long = 0,
    @Required var targetHumiditySoil1: Long = 55,
    @Required var targetHumiditySoil2: Long = 55,
    @Required var pump1OnDuration: Long = 5,
    @Required var pump2OnDuration: Long = 5,
    @Required var waterLevel: Long = 0,
    @Required var automaticMode: Long = 1,
    @Required var lightOnTime: Long = 4,
    @Required var lightOffTime: Long = 24,
    @Required var pumpInterval: Long = 1,
    @Required var plantName1: String = "Name 1",
    @Required var plantName2: String = "Name 2",
    @Required var plantDesc1: String = "Desc 1",
    @Required var plantDesc2: String = "Desc 2",
    @Required var plantDate1: String = "2024-09-22",
    @Required var plantDate2: String = "2024-09-22",

    var changed: Boolean = false,
    var takeImageNow: Boolean = false
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

        this.plantName1 = other.plantName1
        this.plantName2 = other.plantName2
        this.plantDesc1 = other.plantDesc1
        this.plantDesc2 = other.plantDesc2
        this.plantDate1 = other.plantDate1
        this.plantDate2 = other.plantDate2
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
            "Soil Humidity 2" to "$humiditySoil2",
            "Target Soil Humidity 1" to "$targetHumiditySoil1",
            "Target Soil Humidity 2" to "$targetHumiditySoil2",
            "Pump1 duration" to "$pump1OnDuration",
            "Pump2 duration" to "$pump2OnDuration",
            "Automatic Mode" to "$automaticMode",
            "Light On Time" to "$lightOnTime",
            "Pump Interval" to "$pumpInterval",
            "Pump Interval" to "$pumpInterval",
            "plantName1" to plantName1,
            "plantName2" to plantName2,
            "plantDesc1" to plantDesc1,
            "plantDesc2" to plantDesc2,
            "plantDate1" to plantDate1,
            "plantDate2" to plantDate2
        )
    }
}