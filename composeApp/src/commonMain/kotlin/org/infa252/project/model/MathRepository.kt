package org.infa252.project

class MathRepository {

    private val nativeLib = NativeLib()

    fun solve(formula: String, variables: Map<String, String> = emptyMap()): String {
        if (formula.isBlank()) return "0"

        return try {
            val result = nativeLib.calculate(formula, variables)
            if (result.isEmpty()) "0" else result
        } catch (e: Exception) {
            "Ошибка ядра"
        }
    }
}