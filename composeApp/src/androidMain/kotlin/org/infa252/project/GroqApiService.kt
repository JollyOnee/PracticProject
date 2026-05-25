package org.infa252.project

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*

import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream

object GroqApiService {
    private const val API_KEY = "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"
    private const val MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun extractLatex(bitmap: Bitmap): String {
        val base64Image = bitmapToBase64(bitmap)

        val requestBody = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", 1024)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            }
                        }
                        addJsonObject {
                            put("type", "text")
                            put("text", """
                           You are a mathematical OCR-to-LaTeX engine.

Rules:
- Return ONLY valid LaTeX.
- No explanations.
- No markdown.
- No code blocks.
- No surrounding text.
- Output exactly one mathematical expression.
- Use standard LaTeX syntax compatible with KaTeX and MathJax.
- Use \frac{}{} for fractions.
- Use \sqrt{} for roots.
- Use ^{} for powers.
- Use _{} for subscripts.
- Use \int_{a}^{b} ... \, dx for integrals.
- Always wrap function arguments in parentheses: \sin(x), \cos(x), \ln(x)
- Never use Unicode math symbols.
- Never use dollar signs.
- Never use \left or \right.
- Preserve mathematical structure exactly.
- Never use slash division (/).
- ALWAYS represent division using \frac{}{}.
- Every division must use explicit numerator and denominator braces.
- Do not create unnecessary fractions.
- If expression is unreadable, return: INVALID


                            """.trimIndent())
                        }
                    }
                }
            }
        }

        return try {
            val response: HttpResponse = client.post("https://api.groq.com/openai/v1/chat/completions") {
                header("Authorization", "Bearer $API_KEY")
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }
            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            json["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
                ?.trim()
                ?.removePrefix("$")
                ?.removeSuffix("$")
                ?.removePrefix("\\(")
                ?.removeSuffix("\\)")
                ?.removePrefix("\\[")
                ?.removeSuffix("\\]")
                ?.replace("} dx", "}dx")  // убираем пробел перед dx
                ?.replace(" dx", "dx")
                ?.trim() ?: "Ошибка: пустой ответ"
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}