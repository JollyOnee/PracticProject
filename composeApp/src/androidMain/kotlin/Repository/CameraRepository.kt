package org.infa252.project

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val max_tokens: Int = 1000
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: JsonElement
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>? = null,
    val error: GroqError? = null
)

@Serializable
data class GroqChoice(
    val message: GroqMessageResponse
)

@Serializable
data class GroqMessageResponse(
    val content: String
)

@Serializable
data class GroqError(
    val message: String
)

class CameraRepository {

    private val apiKey = "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"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun recognize(bitmap: Bitmap): String {
        val base64 = bitmapToBase64(bitmap)

        val imageContent = buildJsonArray {
            addJsonObject {
                put("type", "image_url")
                putJsonObject("image_url") {
                    put("url", "data:image/jpeg;base64,$base64")
                }
            }
            addJsonObject {
                put("type", "text")
                put("text", """
                    На изображении математическая формула или выражение.
                    Верни ТОЛЬКО LaTeX код этой формулы, без пояснений, 
                    без markdown блоков (без ```), без ${'$'}${'$'} знаков.
                    Только чистый LaTeX. Например: \frac{x+1}{2} или x^{2}+3x-5
                """.trimIndent())
            }
        }

        val response: GroqResponse = client.post(
            "https://api.groq.com/openai/v1/chat/completions"
        ) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(
                GroqRequest(
                    model = "meta-llama/llama-4-scout-17b-16e-instruct",
                    messages = listOf(
                        GroqMessage(
                            role = "user",
                            content = imageContent
                        )
                    )
                )
            )
        }.body()

        if (response.error != null) {
            throw Exception(response.error.message)
        }

        return response.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?: throw Exception("Формула не распознана")
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}