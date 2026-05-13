package org.infa252.project


expect class NativeLib() {
    fun calculate(expression: String): String
}