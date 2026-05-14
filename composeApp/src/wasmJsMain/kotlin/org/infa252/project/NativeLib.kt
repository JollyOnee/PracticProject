package org.infa252.project

actual class NativeLib actual constructor() {
    actual fun calculate(expression: String): String {
        return "Not supported on Wasm"
    }
}
