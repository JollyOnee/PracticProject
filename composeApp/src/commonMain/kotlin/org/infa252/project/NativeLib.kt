package org.infa252.project

class NativeLib {
    private val parser = MathParser()

    fun calculate(expression: String): String {
        return parser.evaluate(expression)
    }
}
