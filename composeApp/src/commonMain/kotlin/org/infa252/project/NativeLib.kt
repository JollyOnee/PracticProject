package org.infa252.project

class NativeLib {
    private val parser = MathParser()

    fun calculate(expression: String, variables: Map<String, String> = emptyMap()): String {
        return parser.evaluate(expression, variables)
    }
}
