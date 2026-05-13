package org.infa252.project

actual class NativeLib actual constructor() {

    init {
        // Название должно совпадать с именем в CMakeLists.txt
        // Для Android это будет libmath_solver_lib.so
        // Для Windows это будет math_solver_lib.dll
        try {
            System.loadLibrary("math_solver_lib")
        } catch (e: UnsatisfiedLinkError) {
            // На десктопе в процессе разработки здесь может понадобиться
            // System.load("полный/путь/к/библиотеке.dll")
            println("Ошибка загрузки библиотеки: ${e.message}")
        }
    }

    // Ключевое слово external говорит системе, что реализация — в C++
    actual external fun calculate(expression: String): String
}