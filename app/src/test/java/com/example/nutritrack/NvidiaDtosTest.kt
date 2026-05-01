package com.example.nutritrack

import com.example.nutritrack.data.remote.models.NvidiaChatRequest
import com.example.nutritrack.data.remote.models.NvidiaMessage
import com.example.nutritrack.data.remote.models.NvidiaChatResponse
import com.example.nutritrack.data.remote.models.NvidiaChoice
import com.example.nutritrack.data.remote.models.NvidiaAssistantMessage
import com.example.nutritrack.data.remote.models.VisionContentPart
import com.example.nutritrack.data.remote.models.ImageUrlPart
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NvidiaDtosTest {

    private val gson = Gson()

    @Test
    fun `NvidiaChatRequest serializes max_tokens correctly`() {
        val request = NvidiaChatRequest(
            model = "meta/llama-3.3-70b-instruct",
            messages = listOf(NvidiaMessage("user", "Hello")),
            maxTokens = 500
        )
        val json = gson.toJson(request)
        assert(json.contains("\"max_tokens\":500")) {
            "Expected max_tokens in JSON but got: $json"
        }
    }

    @Test
    fun `VisionContentPart serializes image_url correctly`() {
        val part = VisionContentPart(
            type = "image_url",
            imageUrl = ImageUrlPart("data:image/jpeg;base64,abc123")
        )
        val json = gson.toJson(part)
        assert(json.contains("\"image_url\"")) {
            "Expected image_url in JSON but got: $json"
        }
    }

    @Test
    fun `NvidiaChatResponse deserializes choices correctly`() {
        val json = """{"choices":[{"message":{"content":"Hello!"}}]}"""
        val response = gson.fromJson(json, NvidiaChatResponse::class.java)
        assertNotNull(response.choices)
        assertEquals(1, response.choices!!.size)
        assertEquals("Hello!", response.choices[0].message?.content)
    }

    @Test
    fun `NvidiaChatRequest has correct default values`() {
        val request = NvidiaChatRequest(
            model = "test-model",
            messages = emptyList()
        )
        assertEquals(0.2, request.temperature, 0.001)
        assertEquals(800, request.maxTokens)
    }

    @Test
    fun `NvidiaMessage with string content serializes correctly`() {
        val message = NvidiaMessage(role = "user", content = "What is in this image?")
        val json = gson.toJson(message)
        assert(json.contains("\"role\":\"user\"")) { "Missing role: $json" }
        assert(json.contains("What is in this image?")) { "Missing content: $json" }
    }
}
