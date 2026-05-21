package org.infa252.project

import java.io.File

actual class NativeLib actual constructor() {

    actual fun calculate(expression: String): String {
        return try {
            calculateNative(expression)
        } catch (e: UnsatisfiedLinkError) {
            "Ошибка: DLL не найдена"
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    actual fun calculateWithXNative(expression: String, xValue: String): String {
        return try {
            val x = xValue.toDoubleOrNull() ?: 0.0
            MathEvaluator.evaluate(expression, x).toString()
        } catch (e: Exception) {
            "Ошибка вычисления"
        }
    }

    private external fun calculateNative(expression: String): String

    init {
        try {
            val projectDir = System.getProperty("user.dir")
            val libFile = File(projectDir, "math_solver_lib.dll")
            if (libFile.exists()) {
                System.load(libFile.absolutePath)
                println("DLL успешно загружена из: ${libFile.absolutePath}")
            } else {
                System.loadLibrary("math_solver_lib")
            }
        } catch (e: UnsatisfiedLinkError) {
            println("Не удалось найти библиотеку: ${e.message}")
        }
    }
}