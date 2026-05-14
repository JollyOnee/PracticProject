package org.infa252.project

actual class NativeLib {
    actual fun calculate(expression: String): String {
        return calculateNative(expression)
    }

    // Имя должно СТРОГО совпадать с Java_org_infa252_project_NativeLib_calculateNative
    private external fun calculateNative(expression: String): String

    companion object {
        init {
            System.loadLibrary("math_solver_lib")
        }
    }
}