package org.infa252.project


actual class NativeLib {
    actual fun calculate(expression: String): String {
        return calculateNative(expression)
    }

    // Это связь с C++ (native-lib.cpp)
    private external fun calculateNative(expression: String): String

    init {

        java.lang.System.loadLibrary("math_solver_lib")
    }
}