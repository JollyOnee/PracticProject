package org.infa252.project

expect class NativeLib() {
    fun calculate(formula: String): String
    fun calculateWithXNative(formula: String, xValue: String): String
}