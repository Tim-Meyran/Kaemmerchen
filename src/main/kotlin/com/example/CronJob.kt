package com.example

import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class CronJob {

    val timer = Timer()

    init {
        timer.scheduleAtFixedRate(1000, 1000) {
            update()
        }
    }

    private fun update() {
        val now = System.currentTimeMillis()

        println("Update ${now}")
    }
}