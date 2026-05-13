package org.infa252.project

class MathParser {

    enum class TokenType { NUMBER, OP, LPAREN, RPAREN, FUNCTION, POSTFIX }
    data class Token(val type: TokenType, val value: String)

    private fun priority(op: String): Int = when (op) {
        "!" -> 5
        "sqrt", "sin", "cos", "tan", "ln", "log", "asin", "acos", "atan" -> 4
        "^" -> 3
        "*", "/" -> 2
        "+", "-" -> 1
        else -> 0
    }

    private val functions = setOf("sqrt", "sin", "cos", "tan", "ln", "log", "asin", "acos", "atan", "abs", "exp")

    fun tokenize(s: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectNumber = true
        
        while (i < s.length) {
            val c = s[i]
            if (c.isWhitespace()) {
                i++
                continue
            }
            
            if (c == '-' && expectNumber) {
                val sb = StringBuilder("-")
                i++
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    sb.append(s[i++])
                }
                tokens.add(Token(TokenType.NUMBER, sb.toString()))
                expectNumber = false
                continue
            }
            
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    sb.append(s[i++])
                }
                tokens.add(Token(TokenType.NUMBER, sb.toString()))
                expectNumber = false
            } else if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < s.length && s[i].isLetter()) {
                    sb.append(s[i++])
                }
                val name = sb.toString()
                if (functions.contains(name)) {
                    tokens.add(Token(TokenType.FUNCTION, name))
                }
                expectNumber = true
            } else if ("+-*/^()!".contains(c)) {
                val type = when (c) {
                    '(' -> TokenType.LPAREN
                    ')' -> TokenType.RPAREN
                    '!' -> TokenType.POSTFIX
                    else -> TokenType.OP
                }
                tokens.add(Token(type, c.toString()))
                expectNumber = c == '(' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^'
                i++
            } else {
                i++ // Ignore unknown
            }
        }
        return tokens
    }

    fun toRPN(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val ops = mutableListOf<Token>()
        
        for (t in tokens) {
            when (t.type) {
                TokenType.NUMBER -> output.add(t)
                TokenType.POSTFIX -> output.add(t) // Postfix operators go directly to output in simple cases
                TokenType.FUNCTION -> {
                    while (ops.isNotEmpty() && ops.last().value != "(" && priority(ops.last().value) >= priority(t.value)) {
                        output.add(ops.removeAt(ops.size - 1))
                    }
                    ops.add(t)
                }
                TokenType.LPAREN -> ops.add(t)
                TokenType.RPAREN -> {
                    while (ops.isNotEmpty() && ops.last().value != "(") {
                        output.add(ops.removeAt(ops.size - 1))
                    }
                    if (ops.isNotEmpty()) ops.removeAt(ops.size - 1)
                    if (ops.isNotEmpty() && ops.last().type == TokenType.FUNCTION) {
                        output.add(ops.removeAt(ops.size - 1))
                    }
                }
                TokenType.OP -> {
                    while (ops.isNotEmpty() && ops.last().value != "(" && priority(ops.last().value) >= priority(t.value)) {
                        output.add(ops.removeAt(ops.size - 1))
                    }
                    ops.add(t)
                }
            }
        }
        while (ops.isNotEmpty()) {
            output.add(ops.removeAt(ops.size - 1))
        }
        return output
    }

    fun evalRPN(rpn: List<Token>): BigNumber {
        val stack = mutableListOf<BigNumber>()
        for (t in rpn) {
            when (t.type) {
                TokenType.NUMBER -> {
                    stack.add(BigNumber.fromString(t.value))
                }
                TokenType.FUNCTION -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Insufficient operands for ${t.value}")
                    val a = stack.removeAt(stack.size - 1)
                    val res = when (t.value) {
                        "sqrt" -> a.sqrt()
                        "sin" -> a.sin()
                        "cos" -> a.cos()
                        "tan" -> a.tan()
                        "ln" -> a.ln()
                        "log" -> a.log10()
                        "asin" -> a.asin()
                        "acos" -> a.acos()
                        "atan" -> a.atan()
                        "abs" -> a.abs()
                        "exp" -> a.exp()
                        else -> throw IllegalArgumentException("Unknown function: ${t.value}")
                    }
                    stack.add(res)
                }
                TokenType.POSTFIX -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Insufficient operands for ${t.value}")
                    val a = stack.removeAt(stack.size - 1)
                    val res = when (t.value) {
                        "!" -> a.factorial()
                        else -> throw IllegalArgumentException("Unknown postfix: ${t.value}")
                    }
                    stack.add(res)
                }
                TokenType.OP -> {
                    if (stack.size < 2) throw IllegalArgumentException("Insufficient operands for ${t.value}")
                    val b = stack.removeAt(stack.size - 1)
                    val a = stack.removeAt(stack.size - 1)
                    val res = when (t.value) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        "^" -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown op: ${t.value}")
                    }
                    stack.add(res)
                }
                else -> {}
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
        return stack.last()
    }

    fun parseLatex(latex: String): String {
        var s = latex
            .replace("\\pi", "3.14159")
            .replace("\\cdot", "*")
            .replace("\\times", "*")
            .replace("\\div", "/")
            .replace("\\ ", "")
            .replace("\\sin", "sin")
            .replace("\\cos", "cos")
            .replace("\\tan", "tan")
            .replace("\\ln", "ln")
            .replace("\\log", "log")
            .replace("\\arcsin", "asin")
            .replace("\\arccos", "acos")
            .replace("\\arctan", "atan")
            .replace("\\sin^{-1}", "asin")
            .replace("\\cos^{-1}", "acos")
            .replace("\\tan^{-1}", "atan")
            .replace("\\abs", "abs")
            .replace("\\exp", "exp")
            
        // Replace |x| with abs(x)
        val sb = StringBuilder()
        var openAbs = false
        for (char in s) {
            if (char == '|') {
                if (!openAbs) {
                    sb.append("abs(")
                    openAbs = true
                } else {
                    sb.append(")")
                    openAbs = false
                }
            } else {
                sb.append(char)
            }
        }
        s = sb.toString()

        // Replace brackets BEFORE anything else
        s = s.replace("{", "(").replace("}", ")").replace("[", "(").replace("]", ")")

        // Handle \sqrt[n](x) -> (x)^(1/n)
        while (s.contains("\\sqrt(")) { // After bracket replacement it's \sqrt(
             break // simplified for now to avoid hang
        }

        // Handle \frac(a)(b) -> (a)/(b)
        while (s.contains("\\frac")) {
            val index = s.indexOf("\\frac")
            var count: Int
            var firstStart: Int
            var firstEnd = -1
            var secondStart: Int
            var secondEnd = -1
            
            // Find first ( )
            var i = index + 5
            while (i < s.length && s[i] != '(') i++
            firstStart = i
            if (i < s.length) {
                count = 1
                i++
                while (i < s.length && count > 0) {
                    if (s[i] == '(') count++
                    else if (s[i] == ')') count--
                    i++
                }
                firstEnd = i - 1
            }
            
            // Find second ( )
            while (i < s.length && s[i] != '(') i++
            secondStart = i
            if (i < s.length) {
                count = 1
                i++
                while (i < s.length && count > 0) {
                    if (s[i] == '(') count++
                    else if (s[i] == ')') count--
                    i++
                }
                secondEnd = i - 1
            }
            
            if (firstEnd != -1 && secondEnd != -1) {
                val first = s.substring(firstStart, firstEnd + 1)
                val second = s.substring(secondStart, secondEnd + 1)
                s = s.substring(0, index) + "($first)/($second)" + s.substring(secondEnd + 1)
            } else {
                s = s.replaceFirst("\\frac", "") // avoid infinite loop
            }
        }

        // Handle \sqrt(x) -> sqrt(x)
        while (s.contains("\\sqrt")) {
            val index = s.indexOf("\\sqrt")
            var i = index + 5
            while (i < s.length && s[i] != '(') i++
            if (i < s.length) {
                var count = 1
                val start = i
                i++
                while (i < s.length && count > 0) {
                    if (s[i] == '(') count++
                    else if (s[i] == ')') count--
                    i++
                }
                val end = i - 1
                val content = s.substring(start, end + 1)
                s = s.substring(0, index) + "sqrt$content" + s.substring(end + 1)
            } else {
                s = s.replaceFirst("\\sqrt", "sqrt")
            }
        }

        return s.replace("\\", "")
    }

    fun evaluate(expression: String): String {
        return try {
            val latexHandled = parseLatex(expression)
            val tokens = tokenize(latexHandled)
            val rpn = toRPN(tokens)
            var res = evalRPN(rpn).toString()
            
            // Post-processing for rounding artifacts from fractional powers (Double conversion artifacts)
            if (res.contains(".")) {
                 val ninePattern = "999999"
                 val zeroPattern = "000000"
                 if (res.contains(ninePattern)) {
                     val idx = res.indexOf(ninePattern)
                     // If it's near the end and we have a fairly long string, it's likely a Double artifact
                     if (res.length - idx < 12 && res.length > 8) {
                         val prefix = res.substring(0, idx)
                         if (prefix.endsWith(".")) {
                             res = (prefix.substring(0, prefix.length - 1).toLong() + 1).toString()
                         } else {
                             res = prefix.substring(0, prefix.length - 1) + (prefix.last().digitToInt() + 1).toString()
                         }
                     }
                 } else if (res.contains(zeroPattern)) {
                     val idx = res.indexOf(zeroPattern)
                     if (res.length - idx < 12 && res.length > 8) {
                        res = res.substring(0, idx).trimEnd('.')
                     }
                 }
            }
            res
        } catch (e: Exception) {
            "Error"
        }
    }
}
