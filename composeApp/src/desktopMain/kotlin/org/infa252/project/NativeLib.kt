package org.infa252.project

import java.io.File

actual class NativeLib actual constructor() {

    actual fun calculate(formula: String): String {
        return try {
            calculateNative(formula)
        } catch (e: UnsatisfiedLinkError) {
            "Ошибка: DLL не найдена"
        }
    }

    actual fun calculateWithXNative(formula: String, xValue: String): String {
        return try {
            calculateWithXNativeImpl(formula, xValue)
        } catch (e: UnsatisfiedLinkError) {
            "Ошибка: DLL не найдена"
        }
    }

    private external fun calculateNative(formula: String): String
    private external fun calculateWithXNativeImpl(formula: String, xValue: String): String

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
            println("Не удалось найти библиотеку в корнях проекта.")
        }
    }
}