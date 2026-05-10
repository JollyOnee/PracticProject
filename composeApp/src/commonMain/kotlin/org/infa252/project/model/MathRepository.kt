package org.infa252.project

/**
 * Репозиторий для связи с математическим ядром.
 */
class MathRepository {

    // В будущем здесь будет:
    // external fun solveNative(formula: String): String

    fun solve(formula: String): String {
        if (formula.isEmpty()) return "0"

        // Пока ядра нет, имитируем результат
        return "ответ"
    }
}