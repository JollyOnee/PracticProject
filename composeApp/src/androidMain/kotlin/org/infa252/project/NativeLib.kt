package org.infa252.project

actual class NativeLib actual constructor() {
    init {
        System.loadLibrary("math_solver_lib")
    }

    actual fun calculate(formula: String): String {
        return calculateNative(formula)
    }

    actual fun calculateWithXNative(formula: String, xValue: String): String {
        return calculateWithXNativeJNI(formula, xValue)
    }

    private external fun calculateNative(formula: String): String
    private external fun calculateWithXNativeJNI(formula: String, xValue: String): String
}