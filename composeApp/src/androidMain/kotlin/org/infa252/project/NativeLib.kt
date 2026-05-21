package org.infa252.project

actual class NativeLib actual constructor() {
    init {
        System.loadLibrary("math_solver_lib")
    }

    actual fun calculate(expression: String): String {
        return calculateNative(expression)
    }

    actual fun calculateWithXNative(expression: String, xValue: String): String {
        return calculateWithXNativeJNI(expression, xValue)
    }

    private external fun calculateNative(expression: String): String
    private external fun calculateWithXNativeJNI(expression: String, xValue: String): String
}