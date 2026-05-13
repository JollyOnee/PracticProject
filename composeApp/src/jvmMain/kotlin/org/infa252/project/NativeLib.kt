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
            // Пытаемся загрузить из системных путей
            System.loadLibrary("math_solver_lib")
        } catch (e: UnsatisfiedLinkError) {
            try {
                // Если не вышло, пробуем загрузить из ресурсов (для десктопа)
                val libName = "libmath_solver_lib.so"
                val inputStream = NativeLib::class.java.getResourceAsStream("/$libName")
                if (inputStream != null) {
                    val tempFile = java.nio.file.Files.createTempFile("math_solver_lib", ".so").toFile()
                    tempFile.deleteOnExit()
                    java.nio.file.Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                    System.load(tempFile.absolutePath)
                } else {
                    println("Ошибка: библиотека $libName не найдена в ресурсах")
                }
            } catch (ex: Exception) {
                println("Ошибка загрузки библиотеки из ресурсов: ${ex.message}")
            }
        }
    }
}
