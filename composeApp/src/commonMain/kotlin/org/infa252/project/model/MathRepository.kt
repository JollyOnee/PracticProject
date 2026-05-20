package org.infa252.project

class MathRepository {

    private val nativeLib = NativeLib()

    fun solve(formula: String): String {
        if (formula.isBlank()) return "0"
        return try {
            val result = nativeLib.calculate(cleanExpression(formula))
            if (result.isEmpty()) "0" else result
        } catch (e: Exception) {
            "Ошибка ядра"
        }
    }

    companion object {
        fun cleanExpression(formula: String): String {
            return formula
                // Только UI-символы которые C++ не знает
                .replace("÷", "/")        // UI символ деления → /
                .replace("\u00D7", "*")   // × (Unicode) → *
                .trim()
        }
    }
}