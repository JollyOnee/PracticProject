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
        val sb = StringBuilder(formula)
        val safeIndex = cursorIndex.coerceIn(0, formula.length)
        sb.insert(safeIndex, symbol)
        formula = sb.toString()

        // Умное перемещение курсора внутрь скобок {}
        cursorIndex = safeIndex + if (symbol.contains("{}")) {
            symbol.indexOf("{") + 1
        } else {
            symbol.length
        }
    }

    fun onDelete() {
        if (cursorIndex > 0) {
            val sb = StringBuilder(formula)
            sb.deleteAt(cursorIndex - 1)
            formula = sb.toString()
            cursorIndex--
        }
    }

    fun onClear() {
        formula = ""
        cursorIndex = 0
        result = ""
    }

    fun moveCursorLeft() {
        cursorIndex = (cursorIndex - 1).coerceIn(0, formula.length)
    }

    fun moveCursorRight() {
        cursorIndex = (cursorIndex + 1).coerceIn(0, formula.length)
    }

    fun solveFormula() {
        result = repository.solve(formula)
    }
}