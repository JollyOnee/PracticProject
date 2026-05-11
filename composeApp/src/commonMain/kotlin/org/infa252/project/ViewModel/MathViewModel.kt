package org.infa252.project

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class MathViewModel(private val repository: MathRepository) : ViewModel() {

    // Состояния экрана
    var formula by mutableStateOf("")
    var cursorIndex by mutableIntStateOf(0)
    var currentTab by mutableStateOf("±")
    var result by mutableStateOf("")

    // Логика вставки символа
    fun onSymbolClick(symbol: String) {
        if (formula.length >= 300) return

        // Prevent multiple decimal points in a single number
        if (symbol == ".") {
            if (isDecimalPointForbidden()) return
        }

        val sb = StringBuilder(formula)
        val safeIndex = cursorIndex.coerceIn(0, formula.length)
        sb.insert(safeIndex, symbol)
        formula = sb.toString()

        // Умное перемещение курсора внутрь первых скобок {}
        cursorIndex = safeIndex + if (symbol.contains("{}")) {
            symbol.indexOf("{}") + 1
        } else {
            symbol.length
        }
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
            if (formula[cursorIndex - 1].isLetter()) {
                var i = cursorIndex - 1
                while (i >= 0 && formula[i].isLetter()) {
                    i--
                }
                if (i >= 0 && formula[i] == '\\') {
                    deleteCount = cursorIndex - i
                }
            } else if (formula[cursorIndex - 1] == '\\') {
                deleteCount = 1
            }

            val start = (cursorIndex - deleteCount).coerceAtLeast(0)
            repeat(deleteCount) {
                sb.deleteAt(start)
            }
            formula = sb.toString()
            cursorIndex = start
        }
    }

    fun onClear() {
        formula = ""
        cursorIndex = 0
        result = ""
    }

    fun moveCursorLeft() {
        if (cursorIndex <= 0) return
        
        var newIndex = cursorIndex - 1
        // Skip over LaTeX commands like \sin or \frac
        while (newIndex > 0 && isInsideLatexCommand(newIndex)) {
            newIndex--
        }
        cursorIndex = newIndex
    }

    fun moveCursorRight() {
        if (cursorIndex >= formula.length) return
        
        var newIndex = cursorIndex + 1
        // Skip over LaTeX commands
        while (newIndex < formula.length && isInsideLatexCommand(newIndex)) {
            newIndex++
        }
        cursorIndex = newIndex
    }

    private fun isInsideLatexCommand(index: Int): Boolean {
        // If we are at a backslash, we are at the start of a command
        if (formula[index] == '\\') return true
        
        // Check if we are part of an alphabetic command following a backslash
        var i = index - 1
        while (i >= 0 && formula[i].isLetter()) {
            i--
        }
        return i >= 0 && formula[i] == '\\'
    }

    fun solveFormula() {
        result = repository.solve(formula)
    }
}