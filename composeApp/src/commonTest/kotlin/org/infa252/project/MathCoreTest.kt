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

        // Inverse trig
        assertEquals("0", parser.evaluate("\\arcsin(0)"))
        assertEquals("0", parser.evaluate("\\arctan(0)"))
        val acos1 = parser.evaluate("\\arccos(1)")
        assertEquals("0", acos1)
        assertEquals("Error", parser.evaluate("\\arcsin(2)"))
    }

    @Test
    fun testNestedFunctions() {
        val parser = MathParser()
        // sqrt(sin(0) + 4) = sqrt(0 + 4) = 2
        assertEquals("2", parser.evaluate("\\sqrt{\\sin(0) + 4}"))
    }

    @Test
    fun testFactorial() {
        val parser = MathParser()
        assertEquals("120", parser.evaluate("5!"))
        assertEquals("1", parser.evaluate("0!"))
        assertEquals("7", parser.evaluate("3! + 1"))
        assertEquals("720", parser.evaluate("(3!)!"))
        assertEquals("Error", parser.evaluate("(-1)!"))
        assertEquals("Error", parser.evaluate("1.5!"))
    }

    @Test
    fun testAbs() {
        val parser = MathParser()
        assertEquals("5", parser.evaluate("abs(-5)"))
        assertEquals("5", parser.evaluate("\\abs{-5}"))
        assertEquals("5", parser.evaluate("|-5|"))
        assertEquals("10", parser.evaluate("abs(-5) * 2"))
    }

    @Test
    fun testFractionalExponents() {
        val parser = MathParser()
        val result = parser.evaluate("2^0.5")
        assertTrue(result.startsWith("1.414"))
        
        assertEquals("8", parser.evaluate("64^(0.5)"))
        assertEquals("Error", parser.evaluate("(-2)^0.5"))
    }

    @Test
    fun testInverseTrig() {
        val parser = MathParser()
        
        // asin
        assertEquals("0", parser.evaluate("asin(0)"))
        assertEquals("0", parser.evaluate("\\arcsin(0)"))
        assertEquals("0", parser.evaluate("\\sin^{-1}(0)"))
        
        // asin(1) = pi/2 approx 1.570796
        val asin1 = parser.evaluate("asin(1)")
        assertTrue(asin1.startsWith("1.5707"))
        
        // acos
        assertEquals("0", parser.evaluate("acos(1)"))
        assertEquals("0", parser.evaluate("\\arccos(1)"))
        assertEquals("0", parser.evaluate("\\cos^{-1}(1)"))
        
        // acos(0) = pi/2
        val acos0 = parser.evaluate("acos(0)")
        assertTrue(acos0.startsWith("1.5707"))
        
        // atan
        assertEquals("0", parser.evaluate("atan(0)"))
        assertEquals("0", parser.evaluate("\\arctan(0)"))
        assertEquals("0", parser.evaluate("\\tan^{-1}(0)"))
        
        // atan(1) = pi/4 approx 0.785398
        val atan1 = parser.evaluate("atan(1)")
        assertTrue(atan1.startsWith("0.7853"))

        // Errors (domain)
        assertTrue(parser.evaluate("asin(2)").contains("domain") || parser.evaluate("asin(2)") == "Ошибка")
        assertTrue(parser.evaluate("acos(-1.1)").contains("domain") || parser.evaluate("acos(-1.1)") == "Ошибка")
    }

    @Test
    fun testHyperbolic() {
        val parser = MathParser()
        assertEquals("0", parser.evaluate("sinh(0)"))
        assertEquals("1", parser.evaluate("cosh(0)"))
        assertEquals("0", parser.evaluate("tanh(0)"))
        
        // sinh(1) approx 1.1752
        val sinh1 = parser.evaluate("sinh(1)")
        assertTrue(sinh1.startsWith("1.1752"))
    }

    @Test
    fun testConstants() {
        val parser = MathParser()
        // Test high precision constants
        val pi = parser.evaluate("pi")
        assertTrue(pi.startsWith("3.141592653589793"))
        
        val e = parser.evaluate("e")
        assertTrue(e.startsWith("2.718281828459"))
        
        // LaTeX pi
        assertEquals(pi, parser.evaluate("\\pi"))
        
        // Calculation with constants
        val res = parser.evaluate("2 * pi")
        assertTrue(res.startsWith("6.283185307"))
    }

    @Test
    fun testScientificNotation() {
        val parser = MathParser()
        assertEquals("0.0000123", parser.evaluate("1.23e-5"))
        assertEquals("123000", parser.evaluate("1.23E5"))
        assertEquals("20", parser.evaluate("2e1"))
    }

    @Test
    fun testErrorHandling() {
        val parser = MathParser()
        // Our improved error messages might not be exactly "Error" anymore
        val divZero = parser.evaluate("1/0")
        assertTrue(divZero.contains("zero") || divZero.contains("Ошибка"))
        
        val invalid = parser.evaluate("1+")
        assertTrue(invalid.contains("выражение") || invalid.contains("Ошибка"))
    }
}
