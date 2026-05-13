package org.infa252.project

actual class NativeLib actual constructor() {
    actual fun calculate(expression: String): String {
        return try {
            MathEvaluator.evaluate(expression)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}