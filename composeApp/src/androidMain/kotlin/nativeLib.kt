package org.infa252.project

// Здесь должно быть слово actual и полная реализация
actual class NativeLib actual constructor() {

    actual fun calculate(expression: String): String {
        return calculateNative(expression)
    }

    private external fun calculateNative(expression: String): String

    init {
        java.lang.System.loadLibrary("math_solver_lib")
    }
}