package org.infa252.project

import kotlin.math.*

object MathEvaluator {

    // ── 1. readBraces ────────────────────────────────────────────────────────

    private fun readBraces(s: String, i: IntArray): String {
        if (i[0] >= s.length || s[i[0]] != '{') return ""
        i[0]++ // skip '{'
        val result = StringBuilder()
        var depth = 1
        while (i[0] < s.length && depth > 0) {
            when (s[i[0]]) {
                '{' -> { depth++; result.append(s[i[0]]) }
                '}' -> { depth--; if (depth > 0) result.append(s[i[0]]) }
                else -> result.append(s[i[0]])
            }
            i[0]++
        }
        return result.toString()
    }

    // ── 2. prepareLatex ──────────────────────────────────────────────────────

    fun prepareLatex(s: String): String {
        val result = StringBuilder()
        val i = intArrayOf(0)
        while (i[0] < s.length) {
            val pos = i[0]
            when {
                s.startsWith("\\frac", pos) -> {
                    i[0] += 5
                    val top = readBraces(s, i)
                    val bottom = readBraces(s, i)
                    result.append("(${prepareLatex(top)})/(${prepareLatex(bottom)})")
                }
                s.startsWith("\\left", pos) -> i[0] += 5
                s.startsWith("\\right", pos) -> i[0] += 6
                s.startsWith("\\times", pos) -> { result.append("*"); i[0] += 6 }
                s.startsWith("\\cdot", pos) -> { result.append("*"); i[0] += 5 }
                s.startsWith("\\int", pos) -> {
                    i[0] += 4
                    var a = ""; var b = ""; var func = ""
                    if (i[0] < s.length && s[i[0]] == '_') { i[0]++; a = readBraces(s, i) }
                    if (i[0] < s.length && s[i[0]] == '^') { i[0]++; b = readBraces(s, i) }
                    func = if (i[0] < s.length && s[i[0]] == '{') {
                        readBraces(s, i)
                    } else {
                        val sb = StringBuilder()
                        while (i[0] < s.length && s[i[0]] != 'd' && !s[i[0]].isWhitespace()) {
                            sb.append(s[i[0]]); i[0]++
                        }
                        sb.toString()
                    }
                    result.append("int(${prepareLatex(func)},${prepareLatex(a)},${prepareLatex(b)})")
                    if (i[0] + 1 < s.length && s[i[0]] == 'd' && s[i[0] + 1] == 'x') i[0] += 2
                    i[0]--
                }
                s.startsWith("\\arcsin", pos) -> {
                    i[0] += 7; val inside = readBraces(s, i)
                    result.append("asin(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\arccos", pos) -> {
                    i[0] += 7; val inside = readBraces(s, i)
                    result.append("acos(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\arctan", pos) -> {
                    i[0] += 7; val inside = readBraces(s, i)
                    result.append("atan(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\sinh", pos) -> {
                    i[0] += 5; val inside = readBraces(s, i)
                    result.append("sinh(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\cosh", pos) -> {
                    i[0] += 5; val inside = readBraces(s, i)
                    result.append("cosh(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\tanh", pos) -> {
                    i[0] += 5; val inside = readBraces(s, i)
                    result.append("tanh(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\sin", pos) -> {
                    i[0] += 4; val inside = readBraces(s, i)
                    result.append("sin(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\cos", pos) -> {
                    i[0] += 4; val inside = readBraces(s, i)
                    result.append("cos(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\tan", pos) -> {
                    i[0] += 4; val inside = readBraces(s, i)
                    result.append("tan(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\ln", pos) -> {
                    i[0] += 3; val inside = readBraces(s, i)
                    result.append("ln(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\log", pos) -> {
                    i[0] += 4
                    var base = ""; var arg = ""
                    if (i[0] < s.length && s[i[0]] == '_') { i[0]++; base = readBraces(s, i) }
                    if (i[0] < s.length && s[i[0]] == '{') arg = readBraces(s, i)
                    result.append("log(${prepareLatex(base)},${prepareLatex(arg)})"); i[0]--
                }
                s.startsWith("\\sqrt", pos) -> {
                    i[0] += 5; val inside = readBraces(s, i)
                    result.append("sqrt(${prepareLatex(inside)})"); i[0]--
                }
                s.startsWith("\\pi", pos) -> {
                    result.append("3.14159265358979"); i[0] += 3
                }
                s.startsWith("\\text{rad}", pos) -> {
                    result.append("rad"); i[0] += 10
                }
                s.startsWith("\\sum", pos) -> {
                    i[0] += 4
                    if (i[0] < s.length && s[i[0]] == '_') { i[0]++; readBraces(s, i) }
                    if (i[0] < s.length && s[i[0]] == '^') { i[0]++; readBraces(s, i) }
                    if (i[0] < s.length && s[i[0]] == '{') readBraces(s, i)
                    i[0]--
                }
                s.startsWith("\\lim", pos) -> {
                    i[0] += 4
                    if (i[0] < s.length && s[i[0]] == '_') { i[0]++; readBraces(s, i) }
                    if (i[0] < s.length && s[i[0]] == '{') readBraces(s, i)
                    i[0]--
                }
                (s[pos] == 'A' || s[pos] == 'C') &&
                        pos + 1 < s.length && s[pos + 1] == '_' -> {
                    val func = s[pos]; i[0] += 2
                    val bottom = readBraces(s, i)
                    if (i[0] < s.length && s[i[0]] == '^') i[0]++
                    val top = readBraces(s, i)
                    result.append("$func(${prepareLatex(bottom)},${prepareLatex(top)})"); i[0]--
                }
                s[pos] == '^' -> {
                    result.append("^"); i[0]++
                    if (i[0] < s.length && s[i[0]] == '{') {
                        val power = readBraces(s, i)
                        result.append(prepareLatex(power))
                    }
                    i[0]--
                }
                s[pos] == '{' -> result.append("(")
                s[pos] == '}' -> result.append(")")
                s[pos] == 'e' && (pos + 1 >= s.length || !s[pos + 1].isLetterOrDigit()) ->
                    result.append("2.71828182845904")
                s[pos] == '\\' -> { /* skip lone backslash */ }
                s[pos] == '÷' -> result.append("/")
                s[pos] == '×' -> result.append("*")
                s[pos] == '|' -> { /* skip */ }
                else -> result.append(s[pos])
            }
            i[0]++
        }
        return result.toString()
    }

    // ── 3. Tokenizer ─────────────────────────────────────────────────────────

    private sealed class Token {
        data class Num(val value: Double) : Token()
        data class Op(val op: Char) : Token()
        data class Func(val name: String) : Token()
    }

    private val FUNCS = listOf(
        "asin", "acos", "atan", "sinh", "cosh", "tanh",
        "sqrt", "log", "sin", "cos", "tan", "int", "ln", "rad"
    )

    private fun tokenize(s: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var expectNumber = true
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c.isWhitespace()) { i++; continue }

            if (c == '-' && expectNumber) {
                val num = StringBuilder("-"); i++
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) { num.append(s[i]); i++ }
                tokens.add(Token.Num(num.toString().toDoubleOrNull() ?: Double.NaN))
                expectNumber = false; continue
            }

            if (c.isDigit() || c == '.') {
                val num = StringBuilder()
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) { num.append(s[i]); i++ }
                tokens.add(Token.Num(num.toString().toDoubleOrNull() ?: Double.NaN))
                expectNumber = false; continue
            }

            val fn = FUNCS.firstOrNull { s.startsWith(it, i) }
            if (fn != null) {
                tokens.add(Token.Func(fn)); i += fn.length; expectNumber = true; continue
            }

            if ((c == 'A' || c == 'C') && i + 1 < s.length && s[i + 1] == '(') {
                tokens.add(Token.Func(c.toString())); i++; expectNumber = true; continue
            }

            if (c in "+-*/^()!%,") {
                tokens.add(Token.Op(c))
                expectNumber = c in "(,+-*/^"
                i++; continue
            }

            i++
        }
        return tokens
    }

    // ── 4. Shunting-yard → RPN ───────────────────────────────────────────────

    private fun priority(op: Char) = when (op) {
        '!', '%' -> 4
        '^' -> 3
        '*', '/' -> 2
        '+', '-' -> 1
        else -> 0
    }

    private fun toRPN(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val ops = ArrayDeque<Token>()
        for (t in tokens) {
            when (t) {
                is Token.Num -> output.add(t)
                is Token.Func -> ops.addLast(t)
                is Token.Op -> when (t.op) {
                    '(' -> ops.addLast(t)
                    ',' -> {
                        while (ops.isNotEmpty() && (ops.last() as? Token.Op)?.op != '(')
                            output.add(ops.removeLast())
                    }
                    ')' -> {
                        while (ops.isNotEmpty() && (ops.last() as? Token.Op)?.op != '(')
                            output.add(ops.removeLast())
                        if (ops.isNotEmpty()) ops.removeLast()
                        if (ops.isNotEmpty() && ops.last() is Token.Func)
                            output.add(ops.removeLast())
                    }
                    '!', '%' -> output.add(t)
                    else -> {
                        while (ops.isNotEmpty()) {
                            val top = ops.last()
                            if (top is Token.Op && top.op != '(' &&
                                priority(top.op) >= priority(t.op))
                                output.add(ops.removeLast())
                            else break
                        }
                        ops.addLast(t)
                    }
                }
            }
        }
        while (ops.isNotEmpty()) output.add(ops.removeLast())
        return output
    }

    // ── 5. RPN evaluator ─────────────────────────────────────────────────────

    private fun evalRPN(rpn: List<Token>): Double {
        val st = ArrayDeque<Double>()
        fun pop() = if (st.isNotEmpty()) st.removeLast() else Double.NaN
        for (t in rpn) {
            when (t) {
                is Token.Num -> st.addLast(t.value)
                is Token.Func -> when (t.name) {
                    "sin"  -> st.addLast(sin(pop()))
                    "cos"  -> st.addLast(cos(pop()))
                    "tan"  -> { val a = pop(); st.addLast(if (cos(a) == 0.0) Double.NaN else tan(a)) }
                    "asin" -> { val a = pop(); st.addLast(if (a in -1.0..1.0) asin(a) else Double.NaN) }
                    "acos" -> { val a = pop(); st.addLast(if (a in -1.0..1.0) acos(a) else Double.NaN) }
                    "atan" -> st.addLast(atan(pop()))
                    "sinh" -> st.addLast(sinh(pop()))
                    "cosh" -> st.addLast(cosh(pop()))
                    "tanh" -> st.addLast(tanh(pop()))
                    "ln"   -> { val a = pop(); st.addLast(if (a > 0) ln(a) else Double.NaN) }
                    "sqrt" -> { val a = pop(); st.addLast(if (a >= 0) sqrt(a) else Double.NaN) }
                    "rad"  -> st.addLast(pop() * PI / 180.0)
                    "log"  -> {
                        val arg = pop(); val base = pop()
                        st.addLast(if (arg > 0 && base > 0 && base != 1.0) log(arg, base) else Double.NaN)
                    }
                    "int"  -> { pop(); pop(); pop(); st.addLast(Double.NaN) }
                    "A"    -> { val k = pop(); val n = pop(); st.addLast(permutations(n, k)) }
                    "C"    -> { val k = pop(); val n = pop(); st.addLast(combinations(n, k)) }
                    else   -> st.addLast(Double.NaN)
                }
                is Token.Op -> when (t.op) {
                    '!' -> st.addLast(factorial(pop()))
                    '%' -> st.addLast(pop() / 100.0)
                    else -> {
                        val b = pop(); val a = pop()
                        st.addLast(when (t.op) {
                            '+' -> a + b
                            '-' -> a - b
                            '*' -> a * b
                            '/' -> if (b == 0.0) Double.NaN else a / b
                            '^' -> { val r = a.pow(b); if (r.isNaN() || r.isInfinite()) Double.NaN else r }
                            else -> Double.NaN
                        })
                    }
                }
            }
        }
        return st.lastOrNull() ?: Double.NaN
    }

    // ── 6. Public API ────────────────────────────────────────────────────────

    fun evaluate(expression: String, x: Double = Double.NaN): Double {
        return try {
            var expr = expression
            if (!x.isNaN() && expr.contains('x')) {
                expr = expr.replace("x", "($x)")
            }
            val prepared = prepareLatex(expr)
            val tokens = tokenize(prepared)
            val rpn = toRPN(tokens)
            val result = evalRPN(rpn)
            if (result.isInfinite()) Double.NaN else result
        } catch (_: Exception) {
            Double.NaN
        }
    }

    // ── 7. Math helpers ──────────────────────────────────────────────────────

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) return Double.NaN
        var r = 1.0; for (i in 2..n.toInt()) r *= i; return r
    }

    private fun permutations(n: Double, k: Double): Double {
        if (n < 0 || k < 0 || k > n) return Double.NaN
        var r = 1.0
        for (i in (n - k + 1).toInt()..n.toInt()) r *= i
        return r
    }

    private fun combinations(n: Double, k: Double) = permutations(n, k) / factorial(k)
}