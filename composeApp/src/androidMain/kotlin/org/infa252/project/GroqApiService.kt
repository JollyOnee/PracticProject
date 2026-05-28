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
                                Ты — специализированный движок перевода математических выражений в строгий формат LaTeX, адаптированный под конкретный парсер приложения. Твоя единственная задача — выдать код, который идеально совпадает с внутренним синтаксисом парсера.

Строгие ограничения:
1. Выводи ТОЛЬКО необработанную (raw) строку LaTeX. Ни в коем случае НЕ используй знаки доллара ($ или $$).
2. НЕ оборачивай результат в блоки разметки markdown (например, НЕ используй `latex или ```).
3. НЕ включай никакой разговорный текст, объяснения или примечания.
4. Точно соблюдай специфику синтаксиса парсера (см. примеры): тригонометрические уравнения полностью оборачиваются в фигурные скобки с экранированием '\{' и '\}', внутри скобок используются '\left(' и '\right)'.
5. НЕ экранируй обычные обратные слэши для команд (пиши '\int', '\sin', '\left'), выдавай чистый текстовый поток.

Примеры входных и выходных данных для точного копирования синтаксиса:

Пример 1 (Тригонометрическое уравнение с фигурные скобками парсера):
- Вход: Изображение или текст уравнения "sin(2x+1) = 0.5"
- Вывод: \sin\{\left( 2 x+1\right)=0.5\}

Пример 2 (Определенный интеграл с границами):
- Вход: Изображение интеграла от 2 до 5 от x в кубе по dx
- Вывод: \int_{2}^{5}{x^{3}}dx

Пример 3 (Интеграл с тригонометрической функцией внутри):
- Вход: Изображение интеграла от 0 до 0.5 от sin(x^3) по dx
- Вывод: \int_{0}^{0.5}{\sin{x^{3}}}dx

Пример 4 (Простое линейное или квадратное выражение под интеграл/парсинг):
- Вход: Выражение x в кубе
- Вывод: {x^{3}}
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