package org.infa252.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val nativeLib = remember { NativeLib() }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var points by remember { mutableStateOf<List<Pair<Double, Double?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }
    val baseRange = 20.0
    val safeScale = scale.coerceIn(0.1f, 50f)

    val xRange = baseRange / safeScale
    val xMin = -xRange + offsetX
    val xMax = xRange + offsetX
    val yOffset = offsetY.toDouble()

    val expression = viewModel.formula

    val parsedExpression = remember(expression) {
        normalizeFormulaForGraph(expression)
    }

    val isVerticalAxis = remember(parsedExpression) {
        parsedExpression.startsWith("x=") || parsedExpression.startsWith("x =")
    }

    LaunchedEffect(parsedExpression) {
        isLoading = true

        points = withContext(Dispatchers.Default) {
            computePoints(
                expression = parsedExpression,
                xMin = -20.0,
                xMax = 20.0,
                steps = 240,
                nativeLib = nativeLib,
                isVertical = isVerticalAxis
            )
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "График",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isVerticalAxis) "x = f(y)" else "f(x) = $expression",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val textMeasurer = rememberTextMeasurer()

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.1f, 50f)

                                    val localXRange = baseRange / scale
                                    offsetX -= (pan.x / size.width * localXRange * 2).toFloat()
                                    offsetY += (pan.y / size.height * localXRange * 2).toFloat()
                                }
                            }
                    ) {
                        drawGridSafe(
                            xMin = xMin,
                            xMax = xMax,
                            yOffset = yOffset,
                            textMeasurer = textMeasurer
                        )

                        drawGraphSafe(
                            points = points,
                            xMin = xMin,
                            xMax = xMax,
                            yOffset = yOffset
                        )
                    }

                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )
                    }
                }
            }

            Text(
                text = "Щипок для зума • перетащи для панорамы",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Button(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сбросить вид")
            }
            Button(
                onClick = {

                    points = emptyList()

                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Очистить график")
            }
            Button(
                onClick = {
                    val svgPoints = points
                        .filter { it.second != null }
                        .map { it.first to it.second!! }

                    val svg = createGraphSvg(
                        points = svgPoints,
                        xMin = xMin,
                        xMax = xMax,
                        yOffset = yOffset
                    )

                    val result = saveSvgFile("graph.svg", svg)

                    exportMessage = "График сохранён"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Экспорт SVG")
            }
            if (exportMessage.isNotBlank()) {

                Text(
                    text = exportMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun normalizeFormulaForGraph(formula: String): String {
    var result = formula.replace(" ", "")

    result = result
        .replace(Regex("""\\ln\{\\left\((.*?)\\right\)([-+]\d+(?:\.\d+)?)\}""")) {
            "ln(${it.groupValues[1]})${it.groupValues[2]}"
        }
        .replace(Regex("""\\sin\{\\left\((.*?)\\right\)([-+]\d+(?:\.\d+)?)\}""")) {
            "sin(${it.groupValues[1]})${it.groupValues[2]}"
        }
        .replace(Regex("""\\cos\{\\left\((.*?)\\right\)([-+]\d+(?:\.\d+)?)\}""")) {
            "cos(${it.groupValues[1]})${it.groupValues[2]}"
        }

    result = result
        .replace("\\left", "")
        .replace("\\right", "")
        .replace("\\times", "*")
        .replace("\\cdot", "*")
        .replace("\\ln", "ln")
        .replace("\\sin", "sin")
        .replace("\\cos", "cos")
        .replace("\\tan", "tan")
        .replace("{", "(")
        .replace("}", ")")

    result = result.replace(Regex("""(\d)(x)"""), "$1*$2")

    return result
}

private suspend fun computePoints(
    expression: String,
    xMin: Double,
    xMax: Double,
    steps: Int,
    nativeLib: NativeLib,
    isVertical: Boolean
): List<Pair<Double, Double?>> {
    if (expression.isBlank()) return emptyList()

    val result = mutableListOf<Pair<Double, Double?>>()
    val xs = mutableListOf<Double>()

    val step = (xMax - xMin) / steps

    var currentX = xMin
    var count = 0

    while (count <= steps) {
        xs.add(currentX)
        currentX += step
        count++
    }

    findLnAsymptote(expression)?.let { asymptote ->
        val epsilons = listOf(
            1.0, 0.5, 0.2, 0.1, 0.05,
            0.02, 0.01, 0.005, 0.001,
            0.0005, 0.0001, 0.00001
        )

        for (eps in epsilons) {
            val x = asymptote + eps
            if (x in xMin..xMax) {
                xs.add(x)
            }
        }
    }

    for (x in xs.sorted()) {
        val fastResult = calculateFast(expression, x)

        val yValue = when {
            fastResult.recognized -> fastResult.value
            else -> calculatePointByNative(nativeLib, expression, x)
        }

        if (isVertical) {
            if (yValue != null && yValue.isFinite()) {
                result.add(yValue to x)
            } else {
                result.add(0.0 to null)
            }
        } else {
            result.add(x to yValue?.takeIf { it.isFinite() })
        }
    }

    return result
}

private fun findLnAsymptote(expression: String): Double? {
    val match = Regex("""ln\((.*)\)([-+]\d+(?:\.\d+)?)?""")
        .matchEntire(expression) ?: return null

    val inside = match.groupValues[1].replace(" ", "")

    val valueAtZero = evalSimpleLinear(inside, 0.0) ?: return null
    val valueAtOne = evalSimpleLinear(inside, 1.0) ?: return null

    val b = valueAtZero
    val a = valueAtOne - valueAtZero

    if (a == 0.0) return null

    return -b / a
}

private data class FastResult(
    val recognized: Boolean,
    val value: Double?
)

private fun calculateFast(expression: String, x: Double): FastResult {
    parseLinearLn(expression, x)?.let { return it }
    parseLinearSin(expression, x)?.let { return it }
    parseLinearCos(expression, x)?.let { return it }

    return FastResult(false, null)
}

private fun parseLinearLn(expression: String, x: Double): FastResult? {
    val match = Regex("""ln\((.*)\)([-+]\d+(?:\.\d+)?)?""")
        .matchEntire(expression) ?: return null

    val inside = evalSimpleLinear(match.groupValues[1], x)
        ?: return FastResult(true, null)

    val outside = match.groupValues
        .getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toDoubleOrNull() ?: 0.0

    if (inside <= 0.0) {
        return FastResult(true, null)
    }

    return FastResult(true, ln(inside) + outside)
}

private fun parseLinearSin(expression: String, x: Double): FastResult? {
    val match = Regex("""sin\((.*)\)([-+]\d+(?:\.\d+)?)?""")
        .matchEntire(expression) ?: return null

    val inside = evalSimpleLinear(match.groupValues[1], x)
        ?: return FastResult(true, null)

    val outside = match.groupValues
        .getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toDoubleOrNull() ?: 0.0

    return FastResult(true, sin(inside) + outside)
}

private fun parseLinearCos(expression: String, x: Double): FastResult? {
    val match = Regex("""cos\((.*)\)([-+]\d+(?:\.\d+)?)?""")
        .matchEntire(expression) ?: return null

    val inside = evalSimpleLinear(match.groupValues[1], x)
        ?: return FastResult(true, null)

    val outside = match.groupValues
        .getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toDoubleOrNull() ?: 0.0

    return FastResult(true, cos(inside) + outside)
}

private fun evalSimpleLinear(expr: String, x: Double): Double? {
    return try {
        val cleaned = expr
            .replace(" ", "")
            .replace("\\times", "*")
            .replace("×", "*")

        var result = 0.0
        var i = 0

        val normalized = if (cleaned.startsWith("-")) cleaned else "+$cleaned"

        while (i < normalized.length) {
            val sign = if (normalized[i] == '-') -1.0 else 1.0
            i++

            val start = i

            while (i < normalized.length && normalized[i] != '+' && normalized[i] != '-') {
                i++
            }

            val term = normalized.substring(start, i)

            val value = when {
                term == "x" -> x

                term == "*x" -> x

                term.contains("x") -> {
                    val coefficient = term
                        .replace("*x", "")
                        .replace("x", "")

                    val k = when (coefficient) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> coefficient.toDouble()
                    }

                    k * x
                }

                else -> term.toDouble()
            }

            result += sign * value
        }

        result
    } catch (_: Throwable) {
        null
    }
}

private suspend fun calculatePointByNative(
    nativeLib: NativeLib,
    expression: String,
    x: Double
): Double? {
    return try {
        val answer = NativeSafe
            .calculateWithX(
                nativeLib = nativeLib,
                formula = expression,
                xValue = String.format(Locale.US, "%.4f", x)
            )
            .trim()

        if (
            answer.isBlank() ||
            answer.contains("Ошибка", ignoreCase = true) ||
            answer.contains("Error", ignoreCase = true) ||
            answer.contains("nan", ignoreCase = true) ||
            answer.contains("inf", ignoreCase = true) ||
            answer.contains("Polynomial", ignoreCase = true)
        ) {
            null
        } else {
            answer.toDoubleOrNull()?.takeIf { it.isFinite() }
        }
    } catch (_: Throwable) {
        null
    }
}

private fun DrawScope.drawGridSafe(
    xMin: Double,
    xMax: Double,
    yOffset: Double,
    textMeasurer: TextMeasurer
) {
    val w = size.width
    val h = size.height

    if (w <= 0f || h <= 0f) return

    val xRange = xMax - xMin
    if (xRange <= 0.0 || !xRange.isFinite()) return

    val yRange = xRange * (h / w)
    val yMin = -yRange / 2.0 + yOffset
    val yMax = yRange / 2.0 + yOffset

    fun toScreenX(x: Double): Float =
        ((x - xMin) / xRange * w).toFloat()

    fun toScreenY(y: Double): Float =
        (h - (y - yMin) / (yMax - yMin) * h).toFloat()

    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val axisColor = Color.Gray.copy(alpha = 0.7f)
    val labelColor = Color.Gray

    val step = niceStep(xRange / 10.0)

    var gx = floor(xMin / step) * step
    var gxCount = 0

    while (gx <= xMax && gxCount < 200) {
        val sx = toScreenX(gx)

        if (sx.isFinite()) {
            drawLine(gridColor, Offset(sx, 0f), Offset(sx, h))
        }

        if (abs(gx) > step * 0.1) {
            val measured = textMeasurer.measure(
                AnnotatedString(formatLabel(gx)),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )

            val labelY = safeCoerce(
                toScreenY(0.0) + 4f,
                4f,
                h - measured.size.height - 4f
            )

            drawText(
                measured,
                topLeft = Offset(sx - measured.size.width / 2f, labelY)
            )
        }

        gx += step
        gxCount++
    }

    var gy = floor(yMin / step) * step
    var gyCount = 0

    while (gy <= yMax && gyCount < 200) {
        val sy = toScreenY(gy)

        if (sy.isFinite()) {
            drawLine(gridColor, Offset(0f, sy), Offset(w, sy))
        }

        if (abs(gy) > step * 0.1) {
            val measured = textMeasurer.measure(
                AnnotatedString(formatLabel(gy)),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )

            val labelX = safeCoerce(
                toScreenX(0.0) + 4f,
                4f,
                w - measured.size.width - 4f
            )

            drawText(
                measured,
                topLeft = Offset(labelX, sy - measured.size.height / 2f)
            )
        }

        gy += step
        gyCount++
    }

    val axisY = safeCoerce(toScreenY(0.0), 0f, h)
    drawLine(axisColor, Offset(0f, axisY), Offset(w, axisY), strokeWidth = 2f)

    val axisX = safeCoerce(toScreenX(0.0), 0f, w)
    drawLine(axisColor, Offset(axisX, 0f), Offset(axisX, h), strokeWidth = 2f)
}

private fun DrawScope.drawGraphSafe(
    points: List<Pair<Double, Double?>>,
    xMin: Double,
    xMax: Double,
    yOffset: Double
) {
    val w = size.width
    val h = size.height

    if (w <= 0f || h <= 0f) return

    val xRange = xMax - xMin
    if (xRange <= 0.0 || !xRange.isFinite()) return

    val yRange = xRange * (h / w)
    val yMin = -yRange / 2.0 + yOffset
    val yMax = yRange / 2.0 + yOffset

    fun toScreenX(x: Double): Float =
        ((x - xMin) / xRange * w).toFloat()

    fun toScreenY(y: Double): Float =
        (h - (y - yMin) / (yMax - yMin) * h).toFloat()

    val graphColor = Color(0xFF2196F3)
    val path = Path()

    var penDown = false
    var previousY: Double? = null

    for ((x, y) in points) {
        if (
            y == null ||
            !x.isFinite() ||
            !y.isFinite() ||
            y < yMin - yRange * 2 ||
            y > yMax + yRange * 2
        ) {
            penDown = false
            previousY = null
            continue
        }

        if (previousY != null && abs(y - previousY!!) > yRange) {
            penDown = false
            previousY = null
        }

        val sx = toScreenX(x)
        val sy = toScreenY(y)

        if (!sx.isFinite() || !sy.isFinite()) {
            penDown = false
            previousY = null
            continue
        }

        if (!penDown) {
            path.moveTo(sx, sy)
            penDown = true
        } else {
            path.lineTo(sx, sy)
        }

        previousY = y
    }

    drawPath(path, graphColor, style = Stroke(width = 3f))
}

private fun niceStep(rawStep: Double): Double {
    if (!rawStep.isFinite() || rawStep <= 0.0) return 1.0

    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude

    return when {
        normalized < 1.5 -> magnitude
        normalized < 3.5 -> 2.0 * magnitude
        normalized < 7.5 -> 5.0 * magnitude
        else -> 10.0 * magnitude
    }
}

private fun safeCoerce(value: Float, min: Float, max: Float): Float {
    if (!value.isFinite()) return min

    return if (max >= min) {
        value.coerceIn(min, max)
    } else {
        min
    }
}

private fun formatLabel(value: Double): String {
    if (!value.isFinite()) return ""

    return if (abs(value - value.roundToInt()) < 0.001) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
private fun createGraphSvg(
    points: List<Pair<Double, Double>>,
    xMin: Double,
    xMax: Double,
    yOffset: Double
): String {
    val width = 1200.0
    val height = 800.0

    val xRange = xMax - xMin
    val yRange = xRange * (height / width)

    val yMin = -yRange / 2.0 + yOffset
    val yMax = yRange / 2.0 + yOffset

    fun toSvgX(x: Double): Double {
        return ((x - xMin) / xRange) * width
    }

    fun toSvgY(y: Double): Double {
        return height - ((y - yMin) / (yMax - yMin)) * height
    }

    val polyline = points
        .filter { it.first.isFinite() && it.second.isFinite() }
        .joinToString(" ") {
            "${toSvgX(it.first)},${toSvgY(it.second)}"
        }

    val axisX = toSvgX(0.0)
    val axisY = toSvgY(0.0)

    return """
<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
    <rect width="100%" height="100%" fill="white"/>

    <line x1="0" y1="$axisY" x2="$width" y2="$axisY" stroke="gray" stroke-width="2"/>
    <line x1="$axisX" y1="0" x2="$axisX" y2="$height" stroke="gray" stroke-width="2"/>

    <polyline points="$polyline" fill="none" stroke="blue" stroke-width="3"/>
</svg>
    """.trimIndent()
}