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
                .replace("\\pi", "3.14159265358979")
                .replace("\\times", "*")
                .replace("\\cdot", "*")
                .replace("\\div", "/")
                .replace("÷", "/")
                .replace("\\frac{", "(")
                .replace("}{", ")/(")
                .replace("\\left(", "(")
                .replace("\\right)", ")")
                .replace("\\left[", "[")
                .replace("\\right]", "]")
                .replace("\\sqrt{", "sqrt(")
                .replace("\\ln{", "ln(")
                .replace("\\log{", "log(")
                .replace("\\sin{", "sin(")
                .replace("\\cos{", "cos(")
                .replace("\\tan{", "tan(")
                .replace("\\%", "%")
                .replace("{", "(")
                .replace("}", ")")
                .replace("\\", "")
                .trim()
        }
    }
}