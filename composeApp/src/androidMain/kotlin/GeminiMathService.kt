package org.infa252.project

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiMathService {
   // private val apiKey: String = "AIzaSyB5wgp_2ATX0CLZIvWAEXv-3BpyC1IoGY4"

    private val model: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-lite",
        apiKey = "AIzaSyB5wgp_2ATX0CLZIvWAEXv-3BpyC1IoGY4"
    )

    suspend fun recognizeExpression(bitmap: Bitmap): String {
        return try {
            val inputContent = content {
                image(bitmap)
                text("Перводи любое мое матПроанализируй эту фотографию. На ней изображена рукописная математическая формула. Твоя задача — точно распознать её и перевести в формат LaTeX. Выводи ответ ТОЛЬКО в блоке кода latex. Не пиши никаких пояснений, комментариев или")
            }
            val response = model.generateContent(inputContent)
            val rawText = response.text ?: ""

            val cleanLatex = rawText
                .replace("```latex", "")
                .replace("\n```", "")
                .trim()

            cleanLatex
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}