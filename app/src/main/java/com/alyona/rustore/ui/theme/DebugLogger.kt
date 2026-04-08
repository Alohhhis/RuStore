package com.alyona.rustore.ui.theme

import java.io.File
import kotlin.random.Random

object DebugLogger {
    private const val LOG_PATH = "d:\\RuStore\\.cursor\\debug.log"

    fun log(
        runId: String,
        hypothesisId: String,
        location: String,
        message: String,
        data: String = "{}"
    ) {
        try {
            val timestamp = System.currentTimeMillis()
            val id = "log_${timestamp}_${Random.nextInt(1000, 9999)}"
            val line =
                """{"id":"$id","timestamp":$timestamp,"location":"$location","message":"$message","data":$data,"runId":"$runId","hypothesisId":"$hypothesisId"}"""
            File(LOG_PATH).appendText("$line\n")
        } catch (_: Exception) {
        }
    }
}
