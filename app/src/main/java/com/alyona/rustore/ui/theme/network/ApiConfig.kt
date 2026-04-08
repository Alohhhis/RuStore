package com.alyona.rustore.ui.theme.network

/**
 * Единая точка для baseUrl и сборки абсолютных URL.
 *
 * Почему нужно:
 * - бэк часто отдаёт относительные пути вида "/images/.."
 * - Glide/OkHttp ожидают абсолютный URL
 * - если "прибивать" базу строкой в каждом адаптере/экране — легко получить рассинхрон
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * Превращает:
     * - "/images/x.png" -> "http://10.0.2.2:8080/images/x.png"
     * - "images/x.png" -> "http://10.0.2.2:8080/images/x.png"
     * - "http(s)://..." -> оставляет как есть
     */
    fun absoluteUrl(pathOrUrl: String): String {
        val trimmed = pathOrUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        val path = if (trimmed.startsWith("/")) trimmed.drop(1) else trimmed
        return BASE_URL + path
    }
}

