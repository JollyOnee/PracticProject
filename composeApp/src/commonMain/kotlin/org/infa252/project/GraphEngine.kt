package org.infa252.project

import kotlin.math.abs

/**
 * Движок для адаптивного расчета точек графика.
 * Использует KmpTreeMap для хранения и Adaptive Sampling для детализации.
 */
class GraphEngine(private val nativeLib: NativeLib) {

    private val tree = KmpTreeMap<Double, Double>()
    private val maxDepth = 10
    private val curvatureThreshold = 0.05

    /**
     * Вычисляет точки функции в заданном диапазоне.
     */
    fun computePoints(expression: String, xMin: Double, xMax: Double): KmpTreeMap<Double, Double> {
        tree.clear()
        val cleanExpr = cleanExpression(expression)
        if (cleanExpr.isEmpty()) return tree

        // 1. Поиск симметрии (упрощенно относительно x=0)
        val isEven = checkEvenSymmetry(cleanExpr)

        // 2. Первичный скелет (около 40-60 точек для баланса дерева)
        fillSkeleton(cleanExpr, xMin, xMax, 0, 6, isEven)
        
        // 3. Адаптивная дискретизация (проход по интервалам)
        val initialPoints = tree.toList()
        for (i in 0 until initialPoints.size - 1) {
            adaptiveRefine(cleanExpr, initialPoints[i], initialPoints[i+1], 0, isEven)
        }
        
        return tree
    }

    /**
     * Рекурсивно добавляет точки, если функция сильно изгибается.
     */
    private fun adaptiveRefine(
        expr: String, 
        p1: Map.Entry<Double, Double>, 
        p2: Map.Entry<Double, Double>, 
        depth: Int, 
        isEven: Boolean
    ) {
        if (depth >= maxDepth) return

        val xMid = (p1.key + p2.key) / 2.0
        val yMid = calculateY(expr, xMid)
        if (yMid.isNaN()) return

        val yLinear = (p1.value + p2.value) / 2.0
        val deviation = abs(yMid - yLinear)

        // Адаптивный порог: 10% от значения или константа
        val threshold = maxOf(curvatureThreshold, abs(yMid) * 0.1)

        if (deviation > threshold) {
            tree.put(xMid, yMid)
            if (isEven) tree.put(-xMid, yMid)
            
            val midEntry = object : Map.Entry<Double, Double> {
                override val key = xMid
                override val value = yMid
            }
            
            adaptiveRefine(expr, p1, midEntry, depth + 1, isEven)
            adaptiveRefine(expr, midEntry, p2, depth + 1, isEven)
        }
    }

    /**
     * Создает начальную сетку точек методом деления пополам.
     */
    private fun fillSkeleton(expr: String, xMin: Double, xMax: Double, depth: Int, targetDepth: Int, isEven: Boolean) {
        if (depth > targetDepth) return

        val mid = (xMin + xMax) / 2.0
        if (tree.get(mid) == null) {
            val y = calculateY(expr, mid)
            if (!y.isNaN()) {
                tree.put(mid, y)
                if (isEven) tree.put(-mid, y)
            }
        }

        fillSkeleton(expr, xMin, mid, depth + 1, targetDepth, isEven)
        fillSkeleton(expr, mid, xMax, depth + 1, targetDepth, isEven)
    }

    private fun calculateY(expr: String, x: Double): Double {
        return try {
            nativeLib.calculate(expr, mapOf("x" to x.toString())).toDoubleOrNull() ?: Double.NaN
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun checkEvenSymmetry(expr: String): Boolean {
        val y1 = calculateY(expr, 1.0)
        val y2 = calculateY(expr, -1.0)
        if (y1.isNaN() || y2.isNaN()) return false
        return abs(y1 - y2) < 1e-6
    }

    private fun cleanExpression(expression: String): String {
        return if (expression.contains("=")) expression.substringAfter("=").trim() else expression.trim()
    }
}
