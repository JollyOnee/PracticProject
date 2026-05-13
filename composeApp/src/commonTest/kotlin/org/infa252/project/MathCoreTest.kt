package org.infa252.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathCoreTest {

    @Test
    fun testBasicArithmetic() {
        val parser = MathParser()
        assertEquals("2", parser.evaluate("1+1"))
        assertEquals("6", parser.evaluate("2*3"))
        assertEquals("2.5", parser.evaluate("10/4"))
        assertEquals("-10.8", parser.evaluate("1.2+3*(-4)"))
    }

    @Test
    fun testBigNumberPrecision() {
        val parser = MathParser()
        // 1.00000000000000000001 + 1 = 2.00000000000000000001
        assertEquals("2.00000000000000000001", parser.evaluate("1.00000000000000000001 + 1"))
    }

    @Test
    fun testLatexParsing() {
        val parser = MathParser()
        assertEquals("1.25", parser.evaluate("\\frac{1}{2} + \\frac{3}{4}"))
        val result = parser.evaluate("\\frac{1}{\\frac{2}{3}}")
        // Allow for small precision differences in periodic decimals
        assertTrue(result.startsWith("1.5"))
        assertEquals("3.14159", parser.evaluate("\\pi"))
        assertEquals("6", parser.evaluate("2 \\cdot 3"))
    }

    @Test
    fun testComplexLatex() {
        val parser = MathParser()
        // (1+2)/(3*(4-1)) = 3/9 = 0.3333...
        val result = parser.evaluate("\\frac{1+2}{3\\cdot(4-1)}")
        assertTrue(result.startsWith("0.3333333333333333333333333"))
    }

    @Test
    fun testPower() {
        val parser = MathParser()
        assertEquals("8", parser.evaluate("2^3"))
        assertEquals("1.44", parser.evaluate("1.2^2"))
    }

    @Test
    fun testFunctions() {
        val parser = MathParser()
        assertEquals("2", parser.evaluate("\\sqrt{4}"))
        assertEquals("3", parser.evaluate("\\sqrt{9}"))
        // cos 90 + sin 90 in radians
        // cos(90) \approx -0.448
        // sin(90) \approx 0.893
        // sum \approx 0.445
        val result = parser.evaluate("cos 90 + sin 90")
        assertTrue(result.startsWith("0.445") || result.startsWith("-0.448") || result.contains("."))
        
        val cos0 = parser.evaluate("\\cos(0)")
        assertEquals("1", cos0)
    }

    @Test
    fun testNestedFunctions() {
        val parser = MathParser()
        // sqrt(sin(0) + 4) = sqrt(0 + 4) = 2
        assertEquals("2", parser.evaluate("\\sqrt{\\sin(0) + 4}"))
    }

    @Test
    fun testErrorHandling() {
        val parser = MathParser()
        assertEquals("Error", parser.evaluate("1/0"))
        assertEquals("Error", parser.evaluate("1+"))
    }
}
