package com.alyona.rustore.ui.theme.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitInstance {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val contentType = "application/json".toMediaType()

    // --- Логгер для Retrofit / OkHttp ---
    private val logging = HttpLoggingInterceptor { message ->
        android.util.Log.d("RetrofitDebug", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: AppApi by lazy {
        Retrofit.Builder()
            // Для Android Emulator localhost хоста = 10.0.2.2
            .baseUrl(ApiConfig.BASE_URL)
            .client(client) // теперь логгер подключен
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AppApi::class.java)
    }
}