package com.example.nutritrack.data.remote

import com.example.nutritrack.data.remote.models.NvidiaChatRequest
import com.example.nutritrack.data.remote.models.NvidiaChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NvidiaApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: NvidiaChatRequest): NvidiaChatResponse
}
