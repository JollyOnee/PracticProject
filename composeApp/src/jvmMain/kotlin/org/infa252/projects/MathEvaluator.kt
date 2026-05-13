package org.infa252.project

object MathEvaluator {
    fun evaluate(expr: String): String {
        val tokens = tokenize(expr)
        val rpn = toRPN(tokens)
        return evalRPN(rpn)
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Op(val value: Char) : Token()
    }

    private fun priority(op: Char) = when (op) {
        '^' -> 3
        '*', '/' -> 2
        '+', '-' -> 1
        else -> 0
    }

    private fun tokenize(s: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectNumber = true
        while (i < s.length) {
            if (s[i].isWhitespace()) { i++; continue }
            if (s[i] == '-' && expectNumber) {
                var num = "-"
                i++
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) num += s[i++]
                tokens.add(Token.Number(num.toDouble()))
                expectNumber = false
                continue
            }
            if (s[i].isDigit() || s[i] == '.') {
                var num = ""
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) num += s[i++]
                tokens.add(Token.Number(num.toDouble()))
                expectNumber = false
            } else if (s[i] in "+-*/^()") {
                tokens.add(Token.Op(s[i]))
                expectNumber = s[i] in "(+-*/^"
                i++
            } else i++
        }
        return tokens
    }

    private fun toRPN(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val ops = ArrayDeque<Char>()
        for (t in tokens) {
            when (t) {
                is Token.Number -> output.add(t)
                is Token.Op -> {
                    when (t.value) {
                        '(' -> ops.addLast(t.value)
                        ')' -> {
                            while (ops.isNotEmpty() && ops.last() != '(') {
                                output.add(Token.Op(ops.removeLast()))
                            }
                            if (ops.isNotEmpty()) ops.removeLast()
                        }
                        else -> {
                            while (ops.isNotEmpty() && priority(ops.last()) >= priority(t.value)) {
                                output.add(Token.Op(ops.removeLast()))
                            }
                            ops.addLast(t.value)
                        }
                    }
                }
            }
        }
        while (ops.isNotEmpty()) output.add(Token.Op(ops.removeLast()))
        return output
    }

    private fun evalRPN(rpn: List<Token>): String {
        val stack = ArrayDeque<Double>()
        for (t in rpn) {
            when (t) {
                is Token.Number -> stack.addLast(t.value)
                is Token.Op -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    stack.addLast(when (t.value) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> if (b == 0.0) throw ArithmeticException("Division by zero") else a / b
                        '^' -> Math.pow(a, b)
                        else -> throw IllegalArgumentException("Unknown op: ${t.value}")
                    })
                }
            }
        }
        val result = stack.last()
        return if (result == result.toLong().toDouble()) result.toLong().toString()
        else result.toString()
    }
}