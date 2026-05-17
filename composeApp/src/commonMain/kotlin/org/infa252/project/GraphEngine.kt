package org.infa252.project

import kotlin.math.abs

/**
 * Улучшенный движок для адаптивного расчета точек графика.
 * Обеспечивает высокую детализацию при любом масштабе за счет визуального порога.
 */
class GraphEngine(private val nativeLib: NativeLib) {

    private val tree = KmpTreeMap<Double, Double>()

    fun computePoints(expression: String, xMin: Double, xMax: Double): KmpTreeMap<Double, Double> {
        tree.clear()
        val cleanExpr = cleanExpression(expression)
        if (cleanExpr.isEmpty()) return tree

        val xRange = xMax - xMin
        
        // Визуальный порог: отклонение более чем на 0.1% от ширины экрана (примерно 1 пиксель)
        // считается изгибом, требующим новой точки.
        val visualThreshold = if (GraphSettings.useScaleSensitivity) {
            (xRange / 1000.0) * (GraphSettings.curvatureThreshold / 0.05)
        } else {
            GraphSettings.curvatureThreshold
        }

        val isEven = checkEvenSymmetry(cleanExpr)

        // Шаг 1: Первичная сетка. Минимум 120 точек равномерно.
        // Это гарантирует, что мы зацепимся за любые колебания функции.
        val initialStep = xRange / 120.0
        var x = xMin
        while (x <= xMax + initialStep * 0.1) {
            val y = calculateY(cleanExpr, x)
            if (!y.isNaN()) {
                tree.put(x, y)
                if (isEven) tree.put(-x, y)
            }
            x += initialStep
        }
        
        // Шаг 2: Адаптивная доработка интервалов
        val initialPoints = tree.toList()
        for (i in 0 until initialPoints.size - 1) {
            adaptiveRefine(cleanExpr, initialPoints[i], initialPoints[i+1], 0, isEven, visualThreshold)
        }
        
        return tree
    }

    private fun adaptiveRefine(
        expr: String, 
        p1: Map.Entry<Double, Double>, 
        p2: Map.Entry<Double, Double>, 
        depth: Int, 
        isEven: Boolean,
        threshold: Double
    ) {
        if (depth >= GraphSettings.maxDepth) return

        val xMid = (p1.key + p2.key) / 2.0
        val yMid = calculateY(expr, xMid)
        if (yMid.isNaN()) return

        val yLinear = (p1.value + p2.value) / 2.0
        val deviation = abs(yMid - yLinear)

        // Порог: учитываем визуальную точность и относительный порог для малых значений
        val limit = maxOf(threshold, abs(yMid) * GraphSettings.relativeThreshold * (threshold / 0.05))

        if (deviation > limit) {
            tree.put(xMid, yMid)
            if (isEven) tree.put(-xMid, yMid)
            
            val midEntry = object : Map.Entry<Double, Double> {
                override val key = xMid
                override val value = yMid
            }
            adaptiveRefine(expr, p1, midEntry, depth + 1, isEven, threshold)
            adaptiveRefine(expr, midEntry, p2, depth + 1, isEven, threshold)
        }
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
