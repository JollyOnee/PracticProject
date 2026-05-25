package org.infa252.project

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MathViewModel(private val repository: MathRepository) : ViewModel() {

    var formula by mutableStateOf("")
    var cursorIndex by mutableIntStateOf(0)
    var currentTab by mutableStateOf("±")
    var result by mutableStateOf("")

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

    fun onSymbolClick(symbol: String) {
        if (formula.length >= 290) return

        if (symbol == ".") {
            if (isDecimalPointForbidden()) return
        }

        val safeIndex = cursorIndex.coerceIn(0, formula.length)

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

        val insertSymbol = if (symbol == "\\times") " \\times " else symbol

        val sb = StringBuilder(formula)
        sb.insert(safeIndex, insertSymbol)
        formula = sb.toString()

        cursorIndex = safeIndex + when {
            symbol.contains("{}") -> symbol.indexOf("{}") + 1
            symbol.contains("[]") -> symbol.indexOf("[]") + 1
            else -> insertSymbol.length
        }
        saveToHistory()
    }

    private fun isDecimalPointForbidden(): Boolean {
        if (formula.isEmpty()) return false
        val safeIndex = cursorIndex.coerceIn(0, formula.length)

        var i = safeIndex - 1
        while (i >= 0) {
            val char = formula[i]
            if (char == '.') return true
            if (!char.isDigit()) break
            i--
        }

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
        if (isInsideLatexCommandName(index)) return false

        val prev = formula[index - 1]
        val curr = formula[index]

        if ((prev == '}' || prev == ']') && curr == '{') return false
        if (curr == '\\') return true

        if (isSyntaxChar(curr) || isSyntaxChar(prev)) {
            val isBeforeClosing = curr == '}' || curr == ']'
            val isAfterOpening = prev == '{' || prev == '['
            return isAfterOpening || isBeforeClosing
        }

        return true
    }

    private fun isSyntaxChar(c: Char): Boolean =
        c == '{' || c == '}' || c == '[' || c == ']' || c == '\\'

    private fun isInsideLatexCommandName(index: Int): Boolean {
        if (index <= 0 || index >= formula.length) return false
        var i = index - 1
        while (i >= 0 && formula[i].isLetter()) {
            i--
        }
        if (i >= 0 && formula[i] == '\\') {
            return true
        }
        return false
    }

    fun solveFormula() {
        viewModelScope.launch {
            result = "Вычисляется..."
            result = withContext(Dispatchers.IO) {
                try {
                    withTimeout(30_000L) {
                        repository.solve(formula)
                    }
                } catch (e: TimeoutCancellationException) {
                    "Превышено время вычисления"
                } catch (e: Exception) {
                    "Ошибка: ${e.message}"
                }
            }
        }
    }
}