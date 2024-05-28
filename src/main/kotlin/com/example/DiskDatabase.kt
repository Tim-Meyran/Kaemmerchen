package com.example

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sqldelight.hockey.data.Datapoint
import com.example.sqldelight.hockey.data.DatapointQueries

class DiskDatabase {

    private val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db")
    private val database = Database(driver)

    init {
        Database.Schema.create(driver)
    }

    fun getDatapoints(): List<Datapoint> {
        val datapointQueries: DatapointQueries = database.datapointQueries
        return datapointQueries.selectAll().executeAsList()
    }

    fun getDataPointsWithName(): Map<String, String> {
        val datapointQueries: DatapointQueries = database.datapointQueries
        val latest = datapointQueries.latest().executeAsOneOrNull()
        return latest?.let {
            mapOf(
                "Mistifier" to "${it.mistifier}",
                "Light" to "${it.light}",
                "Fan 1" to "${it.fan1}",
                "Fan 2" to "${it.fan2}",
                "Pump 1" to "${it.pump1}",
                "Pump 2" to "${it.pump2}",
                "Temperature" to "${it.temperature}",
                "Humidity" to "${it.humidity}",
                "WaterLevel" to "${it.waterLevel}",
                "Soil Humidity 1" to "${it.soilHumidity1}",
                "Soil Humidity 2" to "${it.soilHumidity2}"
            )
        } ?: mapOf()
    }

    fun doDatabaseThings() {
        //val playerQueries: PlayerQueries = database.playerQueries

        /*database.playerQueries.transaction {

        }*/

        //println(playerQueries.selectAll().executeAsList())
        // [HockeyPlayer(15, "Ryan Getzlaf")]

        //playerQueries.insert(player_number = 10, full_name = "Corey Perry")
        //println(playerQueries.selectAll().executeAsList())
        // [HockeyPlayer(15, "Ryan Getzlaf"), HockeyPlayer(10, "Corey Perry")]

        //val player = HockeyPlayer(10, "Ronald McDonald")
        //playerQueries.insertFullPlayerObject(player)
    }
}