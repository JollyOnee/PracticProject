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
                            put(
                                "text",
                                """
                                You are a mathematical OCR-to-LaTeX engine.

                                Return ONLY one LaTeX expression.
                                No explanations.
                                No markdown.
                                No code blocks.
                                No dollar signs.
                                No surrounding text.

                                Rules:
                                - Use standard LaTeX.
                                - Use \frac{}{} for fractions.
                                - Use \sqrt{} for roots.
                                - Use ^{} for powers.
                                - Use _{} for subscripts.
                                - Use \times for multiplication.
                                - Use \int_{a}^{b} ... dx for integrals.
                                - Do not use Unicode math symbols.
                                - Do not use slash division (/).
                                - Do not use \left or \right.
                                - For functions return simple format:
                                  \ln(3 \times x+1)-0.1
                                  \sin(2 \times x+1)-0.5
                                  \cos(3 \times x)
                                  \tan(x)
                                - If expression is unreadable, return INVALID
                                """.trimIndent()
                            )
                        }
                    }
                }
            }
        }

        return try {
            val response: HttpResponse =
                client.post("https://api.groq.com/openai/v1/chat/completions") {
                    header("Authorization", "Bearer $API_KEY")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }

            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                return "Ошибка API: ${response.status.value}"
            }

            val json = Json.parseToJsonElement(body).jsonObject

            val rawLatex = json["choices"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("message")
                ?.jsonObject
                ?.get("content")
                ?.jsonPrimitive
                ?.content
                ?.trim()

            if (rawLatex.isNullOrBlank()) {
                return "Ошибка: пустой ответ"
            }

            normalizeScannerLatex(rawLatex)

        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun normalizeScannerLatex(input: String): String {
        var s = input.trim()

        s = s
            .removePrefix("$")
            .removeSuffix("$")
            .removePrefix("\\(")
            .removeSuffix("\\)")
            .removePrefix("\\[")
            .removeSuffix("\\]")
            .trim()

        s = s.replace("} dx", "}dx")
        s = s.replace(" dx", "dx")

        s = s.replace(Regex("""(\d)\s*x"""), "$1 \\\\times x")

        val functions = listOf("ln", "sin", "cos", "tan")

        for (functionName in functions) {
            s = normalizeFunction(s, functionName)
        }

        return s.trim()
    }

    private fun normalizeFunction(input: String, functionName: String): String {
        val regex = Regex("""\\$functionName\s*\((.*?)\)(.*)$""")
        val match = regex.find(input) ?: return input

        val inside = match.groupValues[1].trim()
        val after = match.groupValues[2].trim()

        return if (after.isNotEmpty()) {
            "\\$functionName{\\left($inside\\right)$after}"
        } else {
            "\\$functionName{\\left($inside\\right)}"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}