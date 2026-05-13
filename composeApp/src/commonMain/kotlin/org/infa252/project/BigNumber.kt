package org.infa252.project

import kotlin.math.max

class BigNumber {
    var value: String
    var point: Int
    var sign: Boolean // true for negative

    constructor(_value: String, _point: Int, _sign: Boolean) {
        this.value = _value
        this.point = _point
        this.sign = _sign
        normalize()
    }

    constructor(_value: String, _point: Int) : this(_value, _point, false)
    constructor(_value: String) : this(_value, 0)
    constructor(number: BigNumber) : this(number.value, number.point, number.sign)

    private fun normalize() {
        // Ensure value ONLY contains digits
        var v = value.filter { it.isDigit() }
        v = v.replaceFirst("^0+".toRegex(), "")
        if (v.isEmpty()) v = "0"
        
        if (v == "0") {
            value = "0"
            sign = false
            point = 0
            return
        }
        
        var p = point
        while (p > 0 && v.endsWith('0')) {
            v = v.substring(0, v.length - 1)
            p--
        }
        value = v
        point = p
    }

    operator fun unaryMinus(): BigNumber {
        return BigNumber(value, point, !sign)
    }

    operator fun plus(other: BigNumber): BigNumber {
        val a = BigNumber(this)
        val b = BigNumber(other)

        while (a.point < b.point) {
            a.value += "0"
            a.point++
        }
        while (b.point < a.point) {
            b.value += "0"
            b.point++
        }
        while (a.value.length < b.value.length) {
            a.value = "0" + a.value
        }
        while (b.value.length < a.value.length) {
            b.value = "0" + b.value
        }

        if (a.sign == b.sign) {
            val sum = StringBuilder()
            var next = 0
            for (i in a.value.length - 1 downTo 0) {
                val c = (a.value[i] - '0') + (b.value[i] - '0') + next
                next = c / 10
                sum.insert(0, (c % 10).toString())
            }
            if (next > 0) {
                sum.insert(0, "1")
            }
            return BigNumber(sum.toString(), a.point, a.sign)
        }

        // Different signs: subtraction
        var aBigger = false
        if (a.value > b.value) {
            aBigger = true
        } else if (a.value == b.value) {
            return BigNumber("0", 0, false)
        }

        val res = StringBuilder()
        var next = 0
        if (aBigger) {
            for (i in a.value.length - 1 downTo 0) {
                var c = (a.value[i] - '0') - (b.value[i] - '0') - next
                if (c < 0) {
                    c += 10
                    next = 1
                } else {
                    next = 0
                }
                res.insert(0, c.toString())
            }
            return BigNumber(res.toString(), a.point, a.sign)
        } else {
            for (i in b.value.length - 1 downTo 0) {
                var c = (b.value[i] - '0') - (a.value[i] - '0') - next
                if (c < 0) {
                    c += 10
                    next = 1
                } else {
                    next = 0
                }
                res.insert(0, c.toString())
            }
            return BigNumber(res.toString(), b.point, b.sign)
        }
    }

    operator fun minus(other: BigNumber): BigNumber {
        return this + (-other)
    }

    operator fun times(other: BigNumber): BigNumber {
        val a = this.value
        val b = other.value
        val res = IntArray(a.length + b.length) { 0 }

        for (i in a.length - 1 downTo 0) {
            var carry = 0
            for (j in b.length - 1 downTo 0) {
                val cur = res[i + j + 1] + (a[i] - '0') * (b[j] - '0') + carry
                res[i + j + 1] = cur % 10
                carry = cur / 10
            }
            res[i] += carry
        }

        val resultStr = res.joinToString("").replaceFirst("^0+".toRegex(), "")
        return BigNumber(if (resultStr.isEmpty()) "0" else resultStr, point + other.point, sign != other.sign)
    }

    operator fun div(other: BigNumber): BigNumber {
        if (other.value == "0") throw ArithmeticException("Division by zero")

        val a = BigNumber(this)
        val b = BigNumber(other)

        val maxP = max(a.point, b.point)
        while (a.point < maxP) { a.value += "0"; a.point++ }
        while (b.point < maxP) { b.value += "0"; b.point++ }

        a.point = 0
        b.point = 0

        val result = StringBuilder()
        var current = ""
        for (i in 0 until a.value.length) {
            current += a.value[i]
            current = trimLeadingZeros(current)
            var digit = 0
            while (absGreaterOrEqual(current, b.value)) {
                current = subAbs(current, b.value)
                digit++
            }
            result.append(digit)
        }

        var precision = 30 
        var pointResult = 0
        while (precision-- > 0 && current != "0") {
            current += "0"
            current = trimLeadingZeros(current)
            var digit = 0
            while (absGreaterOrEqual(current, b.value)) {
                current = subAbs(current, b.value)
                digit++
            }
            result.append(digit)
            pointResult++
        }

        return BigNumber(result.toString(), pointResult, sign != other.sign)
    }

    private fun trimLeadingZeros(s: String): String {
        val res = s.replaceFirst("^0+".toRegex(), "")
        return if (res.isEmpty()) "0" else res
    }

    private fun absGreaterOrEqual(x: String, y: String): Boolean {
        val s1 = trimLeadingZeros(x)
        val s2 = trimLeadingZeros(y)
        return if (s1.length != s2.length) s1.length > s2.length else s1 >= s2
    }

    private fun subAbs(x: String, y: String): String {
        var s1 = trimLeadingZeros(x)
        var s2 = trimLeadingZeros(y)
        while (s2.length < s1.length) s2 = "0" + s2
        val res = StringBuilder()
        var carry = 0
        for (i in s1.length - 1 downTo 0) {
            var c = (s1[i] - '0') - (s2[i] - '0') - carry
            if (c < 0) { c += 10; carry = 1 } else { carry = 0 }
            res.insert(0, c.toString())
        }
        return trimLeadingZeros(res.toString())
    }

    fun pow(exp: BigNumber): BigNumber {
        if (exp.point != 0) throw ArithmeticException("Non-integer exponent")
        if (exp.value == "0") return BigNumber("1", 0, false)
        if (exp.sign) throw ArithmeticException("Negative exponent not supported")

        var base = BigNumber(this)
        var exponentStr = exp.value
        var res = BigNumber("1", 0, false)

        fun isOdd(s: String) = (s.last() - '0') % 2 != 0
        fun div2(s: String): String {
            val resSB = StringBuilder()
            var carry = 0
            for (c in s) {
                val num = carry * 10 + (c - '0')
                resSB.append(num / 2)
                carry = num % 2
            }
            return resSB.toString().replaceFirst("^0+".toRegex(), "").let { if (it.isEmpty()) "0" else it }
        }

        while (exponentStr != "0") {
            if (isOdd(exponentStr)) {
                res = res * base
            }
            base = base * base
            exponentStr = div2(exponentStr)
        }

        if (this.sign && isOdd(exp.value)) {
            res.sign = true
        } else {
            res.sign = false
        }
        return res
    }

    fun sqrt(): BigNumber {
        if (sign) throw ArithmeticException("Square root of negative number")
        if (value == "0") return BigNumber("0")
        
        val doubleVal = this.toString().toDoubleOrNull() ?: 1.0
        var x = fromString(kotlin.math.sqrt(doubleVal).toString())
        val two = fromString("2")
        
        repeat(8) {
            if (x.value != "0") {
                x = (x + (this / x)) / two
            }
        }
        return x
    }

    fun sin(): BigNumber {
        val doubleVal = this.toString().toDoubleOrNull() ?: 0.0
        return fromString(kotlin.math.sin(doubleVal).toString())
    }

    fun cos(): BigNumber {
        val doubleVal = this.toString().toDoubleOrNull() ?: 0.0
        return fromString(kotlin.math.cos(doubleVal).toString())
    }

    fun tan(): BigNumber {
        val doubleVal = this.toString().toDoubleOrNull() ?: 0.0
        return fromString(kotlin.math.tan(doubleVal).toString())
    }

    fun ln(): BigNumber {
        val doubleVal = this.toString().toDoubleOrNull() ?: 1.0
        return fromString(kotlin.math.ln(doubleVal).toString())
    }

    fun log10(): BigNumber {
        val doubleVal = this.toString().toDoubleOrNull() ?: 1.0
        return fromString(kotlin.math.log10(doubleVal).toString())
    }

    fun toStringRepresentation(): String {
        if (value == "0") return "0"
        val sb = StringBuilder()
        if (sign) sb.append("-")
        
        if (point == 0) {
            sb.append(value)
        } else {
            if (value.length > point) {
                val dotPos = value.length - point
                sb.append(value.substring(0, dotPos))
                sb.append(".")
                sb.append(value.substring(dotPos))
            } else {
                sb.append("0.")
                repeat(point - value.length) { sb.append("0") }
                sb.append(value)
            }
        }
        
        var res = sb.toString()
        if (res.contains(".")) {
            res = res.trimEnd('0').trimEnd('.')
        }
        return if (res == "-0" || res == "") "0" else res
    }

    override fun toString(): String = toStringRepresentation()

    companion object {
        fun fromString(s: String): BigNumber {
            var valStr = s.trim()
            if (valStr.isEmpty()) return BigNumber("0")
            
            var sign = false
            if (valStr.startsWith("-")) {
                sign = true
                valStr = valStr.substring(1)
            } else if (valStr.startsWith("+")) {
                valStr = valStr.substring(1)
            }
            
            // Basic handling of scientific notation
            if (valStr.contains('E', ignoreCase = true)) {
                return try {
                    val d = valStr.toDouble()
                    if (kotlin.math.abs(d) < 1e-15) BigNumber("0")
                    else BigNumber(d.toLong().toString())
                } catch (e: Exception) {
                    BigNumber("0")
                }
            }

            val dotIndex = valStr.indexOf('.')
            var point = 0
            if (dotIndex != -1) {
                point = valStr.length - 1 - dotIndex
                valStr = valStr.removeRange(dotIndex, dotIndex + 1)
            }
            
            return BigNumber(valStr.filter { it.isDigit() }, point, sign)
        }
    }
}
