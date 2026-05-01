package com.example.nutritrack.data.repository

import androidx.lifecycle.LiveData
import com.example.nutritrack.BuildConfig
import com.example.nutritrack.data.local.DailyGoalDao
import com.example.nutritrack.data.local.FoodEntryDao
import com.example.nutritrack.data.local.entities.DailyGoal
import com.example.nutritrack.data.local.entities.FoodEntry
import com.example.nutritrack.data.remote.NvidiaApiClient
import com.example.nutritrack.data.remote.models.ImageUrlPart
import com.example.nutritrack.data.remote.models.NutritionAnalysis
import com.example.nutritrack.data.remote.models.NvidiaChatRequest
import com.example.nutritrack.data.remote.models.NvidiaChatResponse
import com.example.nutritrack.data.remote.models.NvidiaMessage
import com.example.nutritrack.data.remote.models.VisionContentPart
import org.json.JSONObject
import java.io.IOException
import retrofit2.HttpException

class FoodRepository(
    private val foodEntryDao: FoodEntryDao,
    private val dailyGoalDao: DailyGoalDao
) {
    private val jsonSystemPrompt = """
        Eres un asistente de nutricion. Responde SIEMPRE y SOLO en JSON valido con esta estructura exacta:
        {
          "alimento": "nombre del alimento",
          "cantidad_gramos": 100,
          "calorias": 0,
          "proteinas_g": 0,
          "carbohidratos_g": 0,
          "grasas_g": 0,
          "fibra_g": 0,
          "azucares_g": 0,
          "sodio_mg": 0,
          "confianza": "alta/media/baja"
        }
        Sin markdown, sin texto adicional, sin comillas triples.
    """.trimIndent()

    fun observeEntriesByDate(date: String): LiveData<List<FoodEntry>> = foodEntryDao.observeEntriesByDate(date)

    fun observeDailyGoal(): LiveData<DailyGoal?> = dailyGoalDao.observeGoal()

    suspend fun insertFoodEntry(entry: FoodEntry): Long = foodEntryDao.insertFoodEntry(entry)

    suspend fun getEntriesBetweenDates(startDate: String, endDate: String): List<FoodEntry> =
        foodEntryDao.getEntriesBetweenDates(startDate, endDate)

    suspend fun saveGoal(goal: DailyGoal) = dailyGoalDao.upsertGoal(goal)

    suspend fun ensureDefaultGoal() {
        val current = dailyGoalDao.getGoal()
        if (current == null) {
            dailyGoalDao.upsertGoal(
                DailyGoal(
                    caloriasObjetivo = 2200.0,
                    proteinasObjetivo = 120.0,
                    carbosObjetivo = 250.0,
                    grasasObjetivo = 70.0
                )
            )
        }
    }

    suspend fun analyzeTextMeal(description: String): NutritionAnalysis {
        val request = NvidiaChatRequest(
            model = BuildConfig.NVIDIA_TEXT_MODEL,
            messages = listOf(
                NvidiaMessage(role = "system", content = jsonSystemPrompt),
                NvidiaMessage(
                    role = "user",
                    content = "Analiza esta comida y estima valores nutricionales: $description"
                )
            )
        )
        val response = requestChatWithFriendlyErrors(request)
        val content = response.choices?.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Respuesta vacia del modelo")
        return parseNutrition(content)
    }

    suspend fun analyzeImageMeal(imageBase64: String, optionalText: String?): NutritionAnalysis {
        val visionPrompt = optionalText?.takeIf { it.isNotBlank() }
            ?: "Analiza la imagen del alimento y estima sus valores nutricionales"

        val candidateModels = listOf(
            BuildConfig.NVIDIA_VISION_MODEL,
            "meta/llama-3.2-90b-vision-instruct",
            "meta/llama-3.2-11b-vision-instruct",
            "microsoft/phi-4-multimodal-instruct",
            "microsoft/phi-3-vision-128k-instruct",
            "nvidia/nemotron-nano-12b-v2-vl"
        ).distinct()

        var lastError: Exception? = null

        for (model in candidateModels) {
            val request = NvidiaChatRequest(
                model = model,
                messages = listOf(
                    NvidiaMessage(role = "system", content = jsonSystemPrompt),
                    NvidiaMessage(
                        role = "user",
                        content = listOf(
                            VisionContentPart(type = "text", text = visionPrompt),
                            VisionContentPart(
                                type = "image_url",
                                imageUrl = ImageUrlPart(url = "data:image/jpeg;base64,$imageBase64")
                            )
                        )
                    )
                )
            )

            try {
                val response = requestChatWithFriendlyErrors(request)
                val content = response.choices?.firstOrNull()?.message?.content
                    ?: throw IllegalStateException("Respuesta vacia del modelo")
                return parseNutrition(content)
            } catch (e: HttpException) {
                // 410 means the model is deprecated/removed: try next model.
                if (e.code() == 410) {
                    lastError = e
                    continue
                }
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        throw IllegalStateException(
            "No se pudo analizar la imagen con los modelos disponibles: ${lastError?.message ?: "error desconocido"}",
            lastError
        )
    }

    private suspend fun requestChatWithFriendlyErrors(request: NvidiaChatRequest): NvidiaChatResponse {
        return try {
            NvidiaApiClient.service.chatCompletions(request)
        } catch (e: HttpException) {
            val status = e.code()
            val body = runCatching { e.response()?.errorBody()?.string().orEmpty() }.getOrDefault("")
            val bodySnippet = body.take(300)
            val message = when (status) {
                401, 403 -> "NVIDIA API rechazó la clave (HTTP $status). Revisa NVIDIA_API_KEY y permisos de la cuenta."
                404 -> "Endpoint o modelo no encontrado (HTTP 404). Revisa NVIDIA_BASE_URL y el modelo configurado."
                410 -> "El modelo NVIDIA está retirado (HTTP 410). Cambia a un modelo vigente."
                429 -> "Límite de peticiones alcanzado (HTTP 429). Espera un momento y vuelve a intentarlo."
                in 500..599 -> "NVIDIA API no disponible temporalmente (HTTP $status). Es un fallo del servicio remoto. Inténtalo más tarde."
                else -> "Error HTTP $status al llamar NVIDIA API."
            }
            val details = if (bodySnippet.isNotBlank()) "\nDetalle: $bodySnippet" else ""
            throw IllegalStateException(message + details, e)
        } catch (e: IOException) {
            throw IllegalStateException("Error de red al contactar NVIDIA API. Revisa tu conexión a Internet.", e)
        }
    }

    private fun parseNutrition(content: String): NutritionAnalysis {
        val cleaned = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(cleaned)

        return NutritionAnalysis(
            alimento = json.optString("alimento", "Desconocido"),
            cantidadGramos = json.optDouble("cantidad_gramos", 0.0),
            calorias = json.optDouble("calorias", 0.0),
            proteinasG = json.optDouble("proteinas_g", 0.0),
            carbohidratosG = json.optDouble("carbohidratos_g", 0.0),
            grasasG = json.optDouble("grasas_g", 0.0),
            fibraG = json.optDouble("fibra_g", 0.0),
            azucaresG = json.optDouble("azucares_g", 0.0),
            sodioMg = json.optDouble("sodio_mg", 0.0),
            confianza = json.optString("confianza", "media")
        )
    }
}
