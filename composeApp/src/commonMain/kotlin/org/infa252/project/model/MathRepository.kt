package org.infa252.project

class MathRepository {

    private val nativeLib = NativeLib()

    fun solve(formula: String): String {
        if (formula.isBlank()) return "0"

        return try {
            val result = nativeLib.calculate(formula)
            if (result.isEmpty()) "0" else result
        } catch (e: Exception) {
            "Ошибка ядра"
        }
    }
}