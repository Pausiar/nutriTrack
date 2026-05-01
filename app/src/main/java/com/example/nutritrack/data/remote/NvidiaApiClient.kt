package com.example.nutritrack.data.remote

import com.example.nutritrack.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NvidiaApiClient {
    private val authInterceptor = Interceptor { chain ->
        val apiKey = BuildConfig.NVIDIA_API_KEY.trim()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("NVIDIA_API_KEY vacia. Configura NVIDIA_API_KEY en local.properties o en la variable de entorno NVIDIA_API_KEY y recompila.")
        }

        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(req)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.NVIDIA_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: NvidiaApiService = retrofit.create(NvidiaApiService::class.java)
}
