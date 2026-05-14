package org.infa252.project

actual class NativeLib actual constructor() {
    init {
        try {
            // Загружаем нашу DLL
            System.loadLibrary("math_solver_lib")
        } catch (e: UnsatisfiedLinkError) {
            println("Ошибка загрузки DLL: ${e.message}")
        }
    }

    // Объявляем метод как external, чтобы он искал его в DLL
    actual fun calculate(expression: String): String = calculateNative(expression)

    private external fun calculateNative(expression: String): String
}