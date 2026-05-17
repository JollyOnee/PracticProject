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

    /**
     * Генерирует SVG-код графика с максимальной детализацией.
     * Выполняется долго, рекомендуется запускать в отдельном потоке.
     */
    fun generateSvgContent(
        expression: String,
        xMin: Double,
        xMax: Double,
        yMin: Double,
        yMax: Double,
        width: Int = 1000,
        height: Int = 1000
    ): String {
        val cleanExpr = cleanExpression(expression)
        val hqTree = KmpTreeMap<Double, Double>()
        
        // В режиме экспорта используем фиксированную высокую точность
        val exportThreshold = (xMax - xMin) / 5000.0 
        val hqMaxDepth = 20
        val isEven = checkEvenSymmetry(cleanExpr)

        // 1. Создаем очень плотный скелет (500 точек)
        val step = (xMax - xMin) / 500.0
        var curX = xMin
        while (curX <= xMax + step * 0.1) {
            val y = calculateY(cleanExpr, curX)
            if (!y.isNaN()) {
                hqTree.put(curX, y)
                if (isEven) hqTree.put(-curX, y)
            }
            curX += step
        }

        // 2. Адаптивная доработка (внутренняя функция для этого метода)
        fun refineHq(p1: Map.Entry<Double, Double>, p2: Map.Entry<Double, Double>, depth: Int) {
            if (depth >= hqMaxDepth) return
            val xm = (p1.key + p2.key) / 2.0
            val ym = calculateY(cleanExpr, xm)
            if (ym.isNaN()) return
            
            val yLinear = (p1.value + p2.value) / 2.0
            if (abs(ym - yLinear) > exportThreshold) {
                hqTree.put(xm, ym)
                if (isEven) hqTree.put(-xm, ym)
                
                val midEntry = object : Map.Entry<Double, Double> {
                    override val key = xm
                    override val value = ym
                }
                refineHq(p1, midEntry, depth + 1)
                refineHq(midEntry, p2, depth + 1)
            }
        }

        val initialPoints = hqTree.toList()
        for (i in 0 until initialPoints.size - 1) {
            refineHq(initialPoints[i], initialPoints[i+1], 0)
        }

        // 3. Генерация SVG строки
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
        sb.append("<svg width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        
        // Фон
        sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"white\" />\n")

        fun toSvgX(x: Double) = ((x - xMin) / (xMax - xMin) * width).toFloat()
        fun toSvgY(y: Double) = (height - (y - yMin) / (yMax - yMin) * height).toFloat()

        // Сетка (упрощенно)
        sb.append("  <g stroke=\"#EEEEEE\" stroke-width=\"1\">\n")
        for (i in 0..10) {
            val x = i * width / 10f
            val y = i * height / 10f
            sb.append("    <line x1=\"$x\" y1=\"0\" x2=\"$x\" y2=\"$height\" />\n")
            sb.append("    <line x1=\"0\" y1=\"$y\" x2=\"$width\" y2=\"$y\" />\n")
        }
        sb.append("  </g>\n")

        // Оси
        val axisX = toSvgX(0.0)
        val axisY = toSvgY(0.0)
        sb.append("  <g stroke=\"#888888\" stroke-width=\"2\">\n")
        if (axisX in 0f..width.toFloat()) sb.append("    <line x1=\"$axisX\" y1=\"0\" x2=\"$axisX\" y2=\"$height\" />\n")
        if (axisY in 0f..height.toFloat()) sb.append("    <line x1=\"0\" y1=\"$axisY\" x2=\"$width\" y2=\"$axisY\" />\n")
        sb.append("  </g>\n")

        // График
        sb.append("  <path d=\"")
        var penDown = false
        for (entry in hqTree) {
            val x = entry.key
            val y = entry.value
            if (y.isNaN() || y.isInfinite() || y < yMin - (yMax-yMin) || y > yMax + (yMax-yMin)) {
                penDown = false
                continue
            }
            val sx = toSvgX(x)
            val sy = toSvgY(y)
            if (!penDown) {
                sb.append("M $sx $sy ")
                penDown = true
            } else {
                sb.append("L $sx $sy ")
            }
        }
        sb.append("\" fill=\"none\" stroke=\"#2196F3\" stroke-width=\"2\" stroke-linejoin=\"round\" />\n")
        
        sb.append("</svg>")
        return sb.toString()
    }
}
