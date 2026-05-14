package org.infa252.project

import java.io.File

actual class NativeLib actual constructor() {

    actual fun calculate(expression: String): String {
        return try {
            calculateNative(expression)
        } catch (e: UnsatisfiedLinkError) {
            "Ошибка: DLL не найдена"
        }
    }

    private external fun calculateNative(expression: String): String

    init {
        try {
            // Ищем DLL в корне проекта (где лежит gradlew)
            val projectDir = System.getProperty("user.dir")
            val libFile = File(projectDir, "math_solver_lib.dll")


            if (libFile.exists()) {
                System.load(libFile.absolutePath)
                println("DLL успешно загружена из: ${libFile.absolutePath}")
            } else {
                System.loadLibrary("math_solver_lib")
            }
        } catch (e: UnsatisfiedLinkError) {
            println("Не удалось найти библиотеку в корнях проекта.")
        }
    }
}