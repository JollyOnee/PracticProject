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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.infa252.project.MathViewModel
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

    val baseRange = 20.0
    val xRange = baseRange / scale
    val xMin = -xRange + offsetX
    val xMax = xRange + offsetX
    val yOffset = offsetY.toDouble()

    // Улучшенная логика: строим по Y только если формула начинается с "x=" или содержит только "y"
    val isVerticalAxis = remember(viewModel.formula) {
        val clean = viewModel.formula.replace(" ", "").lowercase()
        (clean.startsWith("x=") || (clean.contains("y") && !clean.contains("x")))
    }

    val points = remember(viewModel.formula, scale, offsetX, offsetY, isVerticalAxis) {
        computePoints(
            expression = viewModel.formula,
            xMin = xMin,
            xMax = xMax,
            steps = 500, // Увеличиваем детализацию
            nativeLib = nativeLib,
            isVertical = isVerticalAxis
        )
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
                            "Back",
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
                    text = if (isVerticalAxis) "x = f(y)" else "f(x) = ${viewModel.formula}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (viewModel.result.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Результат: ${viewModel.result}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
                val textMeasurer = rememberTextMeasurer()
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.1f, 50f)
                                offsetX -= (pan.x / size.width * xRange * 2).toFloat()
                                offsetY += (pan.y / size.height * xRange * 2).toFloat()
                            }
                        }
                        // Поддержка зума колесиком для ПК (commonMain way)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Scroll) {
                                        val delta = event.changes.first().scrollDelta.y
                                        val zoomFactor = if (delta < 0) 1.1f else 0.9f
                                        scale = (scale * zoomFactor).coerceIn(0.1f, 50f)
                                    }
                                }
                            }
                        }
                ) {
                    drawGrid(xMin, xMax, yOffset, textMeasurer)
                    drawGraph(points, xMin, xMax, yOffset)
                }
            }

            Text(
                text = "Щипок/Колесико для зума • перетащи для панорамы",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Button(
                onClick = { scale = 1f; offsetX = 0f; offsetY = 0f },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сбросить вид")
            }
        }
    }
}

private fun computePoints(
    expression: String,
    xMin: Double,
    xMax: Double,
    steps: Int,
    nativeLib: NativeLib,
    isVertical: Boolean
): List<Pair<Double, Double?>> {
    val result = mutableListOf<Pair<Double, Double?>>()
    val step = (xMax - xMin) / steps
    
    // Очищаем формулу от префиксов типа "y=" или "f(x)="
    // Теперь делаем это безопаснее: если есть "=", берем то, что после него.
    // Если "=" нет, берем всю формулу.
    val cleanExpr = if (expression.contains("=")) {
        expression.substringAfter("=").trim()
    } else {
        expression.trim()
    }
    
    val targetVar = if (isVertical) "y" else "x"
    var currentVal = xMin

    for (i in 0..steps) {
        val v = xMin + i * step
        val res = try {
            // Use the new API that accepts variables map
            nativeLib.calculate(cleanExpr, mapOf(targetVar to v.toString())).toDoubleOrNull()
        } catch (e: Exception) {
            null
        }

        if (isVertical) {
            // Если x = f(y), то результат вычисления (res) - это X, а v - это Y
            // Важно: drawGraph ожидает Pair<Double, Double?>, где first - X, second - Y
            result.add((res ?: Double.NaN) to v)
        } else {
            result.add(v to res)
        }
    }
    return result
}

// Вспомогательная функция для форматирования чисел без String.format (для KMP)
private fun formatDouble(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

private fun niceStep(rawStep: Double): Double {
    if (rawStep <= 0.0) return 1.0
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude
    return when {
        normalized < 1.5 -> magnitude
        normalized < 3.5 -> 2.0 * magnitude
        normalized < 7.5 -> 5.0 * magnitude
        else -> 10.0 * magnitude
    }
}

private fun DrawScope.drawGrid(xMin: Double, xMax: Double, yOffset: Double, textMeasurer: TextMeasurer) {
    val w = size.width
    val h = size.height
    val xRange = xMax - xMin
    val yRange = xRange * (h / w)
    val yMin = -yRange / 2.0 + yOffset
    val yMax = yRange / 2.0 + yOffset

    fun toScreenX(x: Double) = ((x - xMin) / xRange * w).toFloat()
    fun toScreenY(y: Double) = (h - (y - yMin) / (yMax - yMin) * h).toFloat()

    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val axisColor = Color.Gray.copy(alpha = 0.7f)
    val labelColor = Color.Gray

    val step = niceStep(xRange / 10.0)

    var gx = floor(xMin / step) * step
    while (gx <= xMax) {
        val sx = toScreenX(gx)
        drawLine(gridColor, Offset(sx, 0f), Offset(sx, h))
        if (abs(gx) > step * 0.1) {
            val label = if (abs(gx - gx.roundToInt()) < 0.001) gx.roundToInt().toString() else formatDouble(gx)
            val measured = textMeasurer.measure(AnnotatedString(label), style = TextStyle(fontSize = 10.sp, color = labelColor))
            val labelY = (toScreenY(0.0) + 4f).coerceIn(4f, h - measured.size.height - 4f)
            drawText(measured, topLeft = Offset(sx - measured.size.width / 2f, labelY))
        }
        gx += step
    }

    var gy = floor(yMin / step) * step
    while (gy <= yMax) {
        val sy = toScreenY(gy)
        drawLine(gridColor, Offset(0f, sy), Offset(w, sy))
        if (abs(gy) > step * 0.1) {
            val label = if (abs(gy - gy.roundToInt()) < 0.001) gy.roundToInt().toString() else formatDouble(gy)
            val measured = textMeasurer.measure(AnnotatedString(label), style = TextStyle(fontSize = 10.sp, color = labelColor))
            val labelX = (toScreenX(0.0) + 4f).coerceIn(4f, w - measured.size.width - 4f)
            drawText(measured, topLeft = Offset(labelX, sy - measured.size.height / 2f))
        }
        gy += step
    }

    val axisY = toScreenY(0.0).coerceIn(0f, h)
    drawLine(axisColor, Offset(0f, axisY), Offset(w, axisY), strokeWidth = 2f)

    val axisX = toScreenX(0.0).coerceIn(0f, w)
    drawLine(axisColor, Offset(axisX, 0f), Offset(axisX, h), strokeWidth = 2f)
}

private fun DrawScope.drawGraph(points: List<Pair<Double, Double?>>, xMin: Double, xMax: Double, yOffset: Double) {
    val w = size.width
    val h = size.height
    val xRange = xMax - xMin
    val yRange = xRange * (h / w)
    val yMin = -yRange / 2.0 + yOffset
    val yMax = yRange / 2.0 + yOffset

    fun toScreenX(x: Double) = ((x - xMin) / xRange * w).toFloat()
    fun toScreenY(y: Double) = (h - (y - yMin) / (yMax - yMin) * h).toFloat()

    val graphColor = Color(0xFF2196F3)
    val path = Path()
    var penDown = false

    for ((x, y) in points) {
        if (y == null || y.isNaN() || y.isInfinite() ||
            y < yMin - yRange * 5 || y > yMax + yRange * 5) {
            penDown = false
            continue
        }
        val sx = toScreenX(x)
        val sy = toScreenY(y)
        if (!penDown) {
            path.moveTo(sx, sy)
            penDown = true
        } else {
            path.lineTo(sx, sy)
        }
    }
    drawPath(path, graphColor, style = Stroke(width = 3f))
}