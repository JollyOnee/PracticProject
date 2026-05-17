package org.infa252.project

import kotlin.math.abs

/**
 * Улучшенный движок для адаптивного расчета точек графика.
 * Обеспечивает высокую детализацию при любом масштабе за счет визуального порога.
 */
class GraphEngine(private val nativeLib: NativeLib) {

    private val tree = KmpTreeMap<Double, Double>()

    private data class SymmetryInfo(val k: Double, val yK: Double, val isOdd: Boolean)

    fun computePoints(expression: String, xMin: Double, xMax: Double): KmpTreeMap<Double, Double> {
        tree.clear()
        val cleanExpr = cleanExpression(expression)
        if (cleanExpr.isEmpty()) return tree

        val xRange = xMax - xMin
        
        val visualThreshold = if (GraphSettings.useScaleSensitivity) {
            (xRange / 1000.0) * (GraphSettings.curvatureThreshold / 0.05)
        } else {
            GraphSettings.curvatureThreshold
        }

        // Поиск произвольного центра симметрии K
        val symmetry = findSymmetry(cleanExpr)

        // Шаг 1: Первичная сетка.
        val initialStep = xRange / 120.0
        
        if (symmetry != null) {
            val k = symmetry.k
            val yK = symmetry.yK
            val isOdd = symmetry.isOdd
            
            // Считаем только одну сторону от центра K до максимально удаленной границы
            val maxDelta = maxOf(abs(xMax - k), abs(xMin - k))
            
            var dx = 0.0
            while (dx <= maxDelta + initialStep * 0.1) {
                val xRight = k + dx
                val xLeft = k - dx
                
                val y = calculateY(cleanExpr, xRight)
                if (!y.isNaN()) {
                    if (xRight >= xMin && xRight <= xMax) tree.put(xRight, y)
                    
                    // Зеркалим точку влево
                    if (xLeft >= xMin && xLeft <= xMax) {
                        val yMirrored = if (isOdd) 2 * yK - y else y
                        tree.put(xLeft, yMirrored)
                    }
                }
                dx += initialStep
            }
        } else {
            var x = xMin
            while (x <= xMax + initialStep * 0.1) {
                val y = calculateY(cleanExpr, x)
                if (!y.isNaN()) {
                    tree.put(x, y)
                }
                x += initialStep
            }
        }
        
        // Шаг 2: Адаптивная доработка интервалов
        val initialPoints = tree.toList()
        for (i in 0 until initialPoints.size - 1) {
            adaptiveRefine(cleanExpr, initialPoints[i], initialPoints[i+1], 0, symmetry, visualThreshold)
        }
        
        return tree
    }

    private fun adaptiveRefine(
        expr: String, 
        p1: Map.Entry<Double, Double>, 
        p2: Map.Entry<Double, Double>, 
        depth: Int, 
        symmetry: SymmetryInfo?,
        threshold: Double
    ) {
        if (depth >= GraphSettings.maxDepth) return

        val xMid = (p1.key + p2.key) / 2.0
        val yMid = calculateY(expr, xMid)
        if (yMid.isNaN()) return

        val yLinear = (p1.value + p2.value) / 2.0
        val deviation = abs(yMid - yLinear)

        val limit = maxOf(threshold, abs(yMid) * GraphSettings.relativeThreshold * (threshold / 0.05))

        if (deviation > limit) {
            tree.put(xMid, yMid)
            
            // Если есть симметрия, зеркалим и адаптивную точку
            if (symmetry != null) {
                val k = symmetry.k
                val isOdd = symmetry.isOdd
                val yK = symmetry.yK
                val mirroredX = 2 * k - xMid
                val mirroredY = if (isOdd) 2 * yK - yMid else yMid
                tree.put(mirroredX, mirroredY)
            }
            
            val midEntry = object : Map.Entry<Double, Double> {
                override val key = xMid
                override val value = yMid
            }
            adaptiveRefine(expr, p1, midEntry, depth + 1, symmetry, threshold)
            adaptiveRefine(expr, midEntry, p2, depth + 1, symmetry, threshold)
        }
    }

    private fun calculateY(expr: String, x: Double): Double {
        return try {
            nativeLib.calculate(expr, mapOf("x" to x.toString())).toDoubleOrNull() ?: Double.NaN
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun findSymmetry(expr: String): SymmetryInfo? {
        // Проверяем несколько возможных центров K (от -10 до 10 с шагом 0.5)
        var kCandidate = -10.0
        while (kCandidate <= 10.0) {
            var isEven = true
            var isOdd = true
            val yK = calculateY(expr, kCandidate)
            if (yK.isNaN()) { kCandidate += 0.5; continue }
            
            val testOffsets = listOf(0.5, 1.0, 2.0, 5.0)
            for (dx in testOffsets) {
                val yPlus = calculateY(expr, kCandidate + dx)
                val yMinus = calculateY(expr, kCandidate - dx)
                
                if (yPlus.isNaN() || yMinus.isNaN()) {
                    isEven = false; isOdd = false; break
                }
                
                if (abs(yPlus - yMinus) > 1e-6) isEven = false
                if (abs(yPlus + yMinus - 2 * yK) > 1e-6) isOdd = false
                
                if (!isEven && !isOdd) break
            }
            
            if (isEven) return SymmetryInfo(kCandidate, yK, false)
            if (isOdd) return SymmetryInfo(kCandidate, yK, true)
            
            kCandidate += 0.5
        }
        return null
    }

    private fun cleanExpression(expression: String): String {
        return if (expression.contains("=")) expression.substringAfter("=").trim() else expression.trim()
    }

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
        val exportThreshold = (xMax - xMin) / 5000.0 
        val hqMaxDepth = 20
        val symmetry = findSymmetry(cleanExpr)
        val step = (xMax - xMin) / 500.0
        
        if (symmetry != null) {
            val k = symmetry.k
            val yK = symmetry.yK
            val isOdd = symmetry.isOdd
            val maxDelta = maxOf(abs(xMax - k), abs(xMin - k))
            
            var dx = 0.0
            while (dx <= maxDelta + step * 0.1) {
                val xR = k + dx
                val xL = k - dx
                val y = calculateY(cleanExpr, xR)
                if (!y.isNaN()) {
                    if (xR >= xMin && xR <= xMax) hqTree.put(xR, y)
                    if (xL >= xMin && xL <= xMax) {
                        val yM = if (isOdd) 2 * yK - y else y
                        hqTree.put(xL, yM)
                    }
                }
                dx += step
            }
        } else {
            var curX = xMin
            while (curX <= xMax + step * 0.1) {
                val y = calculateY(cleanExpr, curX)
                if (!y.isNaN()) hqTree.put(curX, y)
                curX += step
            }
        }

        fun refineHq(p1: Map.Entry<Double, Double>, p2: Map.Entry<Double, Double>, depth: Int) {
            if (depth >= hqMaxDepth) return
            val xm = (p1.key + p2.key) / 2.0
            val ym = calculateY(cleanExpr, xm)
            if (ym.isNaN()) return
            val yLinear = (p1.value + p2.value) / 2.0
            if (abs(ym - yLinear) > exportThreshold) {
                hqTree.put(xm, ym)
                if (symmetry != null) {
                    val mx = 2 * symmetry.k - xm
                    val my = if (symmetry.isOdd) 2 * symmetry.yK - ym else ym
                    hqTree.put(mx, my)
                }
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
        return generateSvgString(hqTree, xMin, xMax, yMin, yMax, width, height)
    }

    private fun generateSvgString(tree: KmpTreeMap<Double, Double>, xMin: Double, xMax: Double, yMin: Double, yMax: Double, width: Int, height: Int): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
        sb.append("<svg width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"white\" />\n")
        fun toSvgX(x: Double) = ((x - xMin) / (xMax - xMin) * width).toFloat()
        fun toSvgY(y: Double) = (height - (y - yMin) / (yMax - yMin) * height).toFloat()
        sb.append("  <g stroke=\"#EEEEEE\" stroke-width=\"1\">\n")
        for (i in 0..10) {
            val x = i * width / 10f
            val y = i * height / 10f
            sb.append("    <line x1=\"$x\" y1=\"0\" x2=\"$x\" y2=\"$height\" />\n")
            sb.append("    <line x1=\"0\" y1=\"$y\" x2=\"$width\" y2=\"$y\" />\n")
        }
        sb.append("  </g>\n")
        val axisX = toSvgX(0.0)
        val axisY = toSvgY(0.0)
        sb.append("  <g stroke=\"#888888\" stroke-width=\"2\">\n")
        if (axisX in 0f..width.toFloat()) sb.append("    <line x1=\"$axisX\" y1=\"0\" x2=\"$axisX\" y2=\"$height\" />\n")
        if (axisY in 0f..height.toFloat()) sb.append("    <line x1=\"0\" y1=\"$axisY\" x2=\"$width\" y2=\"$axisY\" />\n")
        sb.append("  </g>\n")
        sb.append("  <path d=\"")
        var penDown = false
        for (entry in tree) {
            val x = entry.key
            val y = entry.value
            if (y.isNaN() || y.isInfinite()) { penDown = false; continue }
            val sx = toSvgX(x)
            val sy = toSvgY(y)
            if (!penDown) { sb.append("M $sx $sy "); penDown = true } else { sb.append("L $sx $sy ") }
        }
        sb.append("\" fill=\"none\" stroke=\"#2196F3\" stroke-width=\"2\" stroke-linejoin=\"round\" />\n")
        sb.append("</svg>")
        return sb.toString()
    }
}
