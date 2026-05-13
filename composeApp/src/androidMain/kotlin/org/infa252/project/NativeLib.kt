package org.infa252.project

actual class NativeLib actual constructor() {

    actual fun calculate(expression: String): String {
        return try {
            calculateNative(expression)
        } catch (e: UnsatisfiedLinkError) {
            "Error: Native library not loaded"
        }
    }

    private external fun calculateNative(expression: String): String

    init {
        try {
            System.loadLibrary("math_solver_lib")
        } catch (e: UnsatisfiedLinkError) {
            println("Ошибка загрузки библиотеки: ${e.message}")
        }
    }
}
