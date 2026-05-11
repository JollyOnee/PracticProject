package org.infa252.project

class MathRepository {

    private val nativeLib = NativeLib()

    fun solve(formula: String): String {
        if (formula.isBlank()) return "0"

        return try {
            val preparedInput = formula
                .replace("\\pi", "3.14159")
                .replace("\\times", "*")
                .replace("\\div", "/")
                .replace("\\frac{", "(")
                .replace("}{", ")/(")
                .replace("{", "(")
                .replace("}", ")")
                .replace("\\", "")


            val result: String = nativeLib.calculate(preparedInput)

                 if (result.isEmpty()) "0" else result

        } catch (e: Exception) {
            "Ошибка ядра"
        }
    }
}