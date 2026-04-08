package com.alyona.rustore.ui.theme.network

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"

    fun absoluteUrl(pathOrUrl: String): String {
        val trimmed = pathOrUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        val path = if (trimmed.startsWith("/")) trimmed.drop(1) else trimmed
        return BASE_URL + path
    }
}

