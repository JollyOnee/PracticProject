package org.infa252.project

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class MathViewModel(private val repository: MathRepository) : ViewModel() {

    // Состояния экрана
    var formula by mutableStateOf("")
    var cursorIndex by mutableIntStateOf(0)
    var currentTab by mutableStateOf("±")
    var result by mutableStateOf("")

    // История для Undo/Redo
    private val history = mutableListOf<Pair<String, Int>>()
    private var historyIndex = -1

    init {
        saveToHistory()
    }

    private fun saveToHistory() {
        if (historyIndex < history.size - 1) {
            val toRemove = history.size - 1 - historyIndex
            repeat(toRemove) { history.removeAt(history.size - 1) }
        }
        if (history.isNotEmpty() && history.last().first == formula && history.last().second == cursorIndex) return
        history.add(formula to cursorIndex)
        if (history.size > 50) history.removeAt(0)
        historyIndex = history.size - 1
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--
            val state = history[historyIndex]
            formula = state.first
            cursorIndex = state.second
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            val state = history[historyIndex]
            formula = state.first
            cursorIndex = state.second
        }
    }

    // Логика вставки символа
    fun onSymbolClick(symbol: String) {
        if (formula.length >= 290) return

        // Prevent multiple decimal points in a single number
        if (symbol == ".") {
            if (isDecimalPointForbidden()) return
        }

        val safeIndex = cursorIndex.coerceIn(0, formula.length)

        // Smart Fraction: transform "715/" into "\frac{715}{}"
        if (symbol == "/" || symbol == "÷") {
            var start = safeIndex - 1
            while (start >= 0 && (formula[start].isDigit() || formula[start] == '.')) {
                start--
            }
            start++

            val number = formula.substring(start, safeIndex)
            val prefix = formula.substring(0, start)
            val suffix = formula.substring(safeIndex)

            formula = "$prefix\\frac{$number}{}$suffix"
            cursorIndex = prefix.length + "\\frac{$number}{".length
            saveToHistory()
            return
        }

        val sb = StringBuilder(formula)
        sb.insert(safeIndex, symbol)
        formula = sb.toString()

        // Умное перемещение курсора внутрь первых скобок {} или []
        cursorIndex = safeIndex + when {
            symbol.contains("{}") -> symbol.indexOf("{}") + 1
            symbol.contains("[]") -> symbol.indexOf("[]") + 1
            else -> symbol.length
        }
        saveToHistory()
    }

    private fun isDecimalPointForbidden(): Boolean {
        if (formula.isEmpty()) return false
        val safeIndex = cursorIndex.coerceIn(0, formula.length)

        // Look back from cursor to find if the current number already has a dot
        var i = safeIndex - 1
        while (i >= 0) {
            val char = formula[i]
            if (char == '.') return true
            if (!char.isDigit()) break
            i--
        }

        // Look forward from cursor
        var j = safeIndex
        while (j < formula.length) {
            val char = formula[j]
            if (char == '.') return true
            if (!char.isDigit()) break
            j++
        }

        return false
    }

    fun onDelete() {
        if (cursorIndex > 0) {
            val sb = StringBuilder(formula)

            // Smart delete: if deleting a command part, delete the whole command
            var deleteCount = 1
            val prevChar = formula[cursorIndex - 1]

            if (prevChar.isLetter()) {
                var i = cursorIndex - 1
                while (i >= 0 && formula[i].isLetter()) {
                    i--
                }
                if (i >= 0 && formula[i] == '\\') {
                    deleteCount = cursorIndex - i
                }
            } else if (prevChar == '%' || prevChar == '$' || prevChar == '#' || prevChar == '_' || prevChar == '&') {
                // Handle escaped special characters: \% \$ \# \_ \&
                if (cursorIndex >= 2 && formula[cursorIndex - 2] == '\\') {
                    deleteCount = 2
                }
            } else if (prevChar == '\\') {
                deleteCount = 1
            }

            val start = (cursorIndex - deleteCount).coerceAtLeast(0)
            repeat(deleteCount) {
                if (start < sb.length) sb.deleteAt(start)
            }
            formula = sb.toString()
            cursorIndex = start
            saveToHistory()
        }
    }

    fun onClear() {
        formula = ""
        cursorIndex = 0
        result = ""
        saveToHistory()
    }

    fun moveCursorLeft() {
        var nextIndex = cursorIndex - 1
        while (nextIndex >= 0 && !isValidCursorPosition(nextIndex)) {
            nextIndex--
        }
        if (nextIndex >= 0) {
            cursorIndex = nextIndex
        }
    }

    fun moveCursorRight() {
        var nextIndex = cursorIndex + 1
        while (nextIndex <= formula.length && !isValidCursorPosition(nextIndex)) {
            nextIndex++
        }
        if (nextIndex <= formula.length) {
            cursorIndex = nextIndex
        }
    }

    private fun isValidCursorPosition(index: Int): Boolean {
        if (index == 0 || index == formula.length) return true

        // Don't land inside a LaTeX command name (like \fr|ac)
        if (isInsideLatexCommandName(index)) return false

        val prev = formula[index - 1]
        val curr = formula[index]

        // Don't land between syntax characters like }{ or ]{
        if ((prev == '}' || prev == ']') && curr == '{') return false

        // Allow landing before a backslash (start of a command)
        if (curr == '\\') return true

        // Don't land immediately after a backslash or inside other syntax chars
        // except when it's the boundary of an editable area
        if (isSyntaxChar(curr) || isSyntaxChar(prev)) {
            val isBeforeClosing = curr == '}' || curr == ']'
            val isAfterOpening = prev == '{' || prev == '['

            return isAfterOpening || isBeforeClosing
        }

        return true
    }

    private fun isSyntaxChar(c: Char): Boolean = c == '{' || c == '}' || c == '[' || c == ']' || c == '\\'

    private fun isInsideLatexCommandName(index: Int): Boolean {
        if (index <= 0 || index >= formula.length) return false

        // Check backwards for a backslash
        var i = index - 1
        while (i >= 0 && formula[i].isLetter()) {
            i--
        }

        if (i >= 0 && formula[i] == '\\') {
            // It's a command name if all characters from the backslash to index are letters
            return true
        }

        return false
    }

    fun solveFormula() {
        result = repository.solve(formula)
    }
}