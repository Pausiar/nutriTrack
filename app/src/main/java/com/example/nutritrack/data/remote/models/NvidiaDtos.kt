package com.example.nutritrack.data.remote.models

import com.squareup.moshi.Json

data class NvidiaChatRequest(
    val model: String,
    val messages: List<NvidiaMessage>,
    val temperature: Double = 0.2,
    @Json(name = "max_tokens") val maxTokens: Int = 800
)

data class NvidiaMessage(
    val role: String,
    val content: Any
)

data class VisionContentPart(
    val type: String,
    val text: String? = null,
    @Json(name = "image_url") val imageUrl: ImageUrlPart? = null
)

data class ImageUrlPart(
    val url: String
)

data class NvidiaChatResponse(
    val choices: List<NvidiaChoice>?
)

data class NvidiaChoice(
    val message: NvidiaAssistantMessage?
)

data class NvidiaAssistantMessage(
    val content: String?
)

data class NutritionAnalysis(
    val alimento: String,
    val cantidadGramos: Double,
    val calorias: Double,
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val fibraG: Double,
    val azucaresG: Double,
    val sodioMg: Double,
    val confianza: String
)
