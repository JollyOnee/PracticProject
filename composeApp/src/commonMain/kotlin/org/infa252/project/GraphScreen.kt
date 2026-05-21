package org.infa252.project

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var points by remember { mutableStateOf(listOf<Pair<Double, Double?>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val baseRange = 20.0
    val xRange = baseRange / scale
    val xMin = -xRange + offsetX
    val xMax = xRange + offsetX
    val yOffset = offsetY.toDouble()

    val isVerticalAxis = viewModel.formula.contains("y")

    LaunchedEffect(viewModel.formula, scale, offsetX, offsetY) {
        isLoading = true
        points = withContext(Dispatchers.Default) {
            computePoints(
                expression = viewModel.formula,
                xMin = xMin,
                xMax = xMax,
                steps = 300,
                isVertical = isVerticalAxis
            )
        }
        isLoading = false
    }

    if (showSettings) {
        GraphSettingsDialog(onDismiss = { showSettings = false })
    }

    if (showExportDialog) {
        val yRange = (xMax - xMin)
        var exportXMin by remember { mutableStateOf(xMin.toString()) }
        var exportXMax by remember { mutableStateOf(xMax.toString()) }
        var exportYMin by remember { mutableStateOf((-yRange / 2 + yOffset).toString()) }
        var exportYMax by remember { mutableStateOf((yRange / 2 + yOffset).toString()) }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Экспорт в SVG (HQ)") },
            text = {
                Column {
                    Text(
                        "Настройте область для экспорта:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = exportXMin,
                            onValueChange = { exportXMin = it },
                            label = { Text("xMin") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = exportXMax,
                            onValueChange = { exportXMax = it },
                            label = { Text("xMax") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = exportYMin,
                            onValueChange = { exportYMin = it },
                            label = { Text("yMin") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = exportYMax,
                            onValueChange = { exportYMax = it },
                            label = { Text("yMax") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        "Внимание: расчет с максимальной детализацией может занять время.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val x1 = exportXMin.toDoubleOrNull() ?: xMin
                    val x2 = exportXMax.toDoubleOrNull() ?: xMax
                    val y1 = exportYMin.toDoubleOrNull() ?: -10.0
                    val y2 = exportYMax.toDoubleOrNull() ?: 10.0

                    showExportDialog = false
                    isExporting = true

                    scope.launch {
                        val svg = withContext(Dispatchers.Default) {
                            generateSvgContent(viewModel.formula, x1, x2, y1, y2)
                        }
                        saveSvg("graph_${viewModel.formula.replace("\\s".toRegex(), "")}.svg", svg)
                        isExporting = false
                    }
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (isExporting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Генерация HQ графика...", color = Color.White)
            }
        }
        return
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
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            Icons.Default.Download,
                            "Export SVG",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Настройки графика",
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
                Box(modifier = Modifier.fillMaxSize()) {
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

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сбросить вид")
            }
        }
    }
}

// ---------- SVG Export ----------

private fun generateSvgContent(
    expression: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    width: Int = 1000,
    height: Int = 1000
): String {
    val cleanExpr = if (expression.contains("=")) expression.substringAfter("=").trim()
    else expression.trim()

    // HQ: 1000 начальных шагов + адаптивное уточнение до глубины 20
    val hqPoints = computePoints(
        expression = cleanExpr,
        xMin = xMin,
        xMax = xMax,
        steps = 1000,
        isVertical = false
    )

    fun toSvgX(x: Double) = ((x - xMin) / (xMax - xMin) * width).toFloat()
    fun toSvgY(y: Double) = (height - (y - yMin) / (yMax - yMin) * height).toFloat()

    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    sb.append("<svg width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\" xmlns=\"http://www.w3.org/2000/svg\">\n")
    sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"white\" />\n")

    // Сетка
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
    if (axisX in 0f..width.toFloat())
        sb.append("    <line x1=\"$axisX\" y1=\"0\" x2=\"$axisX\" y2=\"$height\" />\n")
    if (axisY in 0f..height.toFloat())
        sb.append("    <line x1=\"0\" y1=\"$axisY\" x2=\"$width\" y2=\"$axisY\" />\n")
    sb.append("  </g>\n")

    // Кривая
    sb.append("  <path d=\"")
    var penDown = false
    for ((x, y) in hqPoints) {
        if (y == null || y.isNaN() || y.isInfinite()) { penDown = false; continue }
        val sx = toSvgX(x)
        val sy = toSvgY(y)
        if (!penDown) { sb.append("M $sx $sy "); penDown = true }
        else          { sb.append("L $sx $sy ") }
    }
    sb.append("\" fill=\"none\" stroke=\"#2196F3\" stroke-width=\"2\" stroke-linejoin=\"round\" />\n")
    sb.append("</svg>")
    return sb.toString()
}

// ---------- Остальной код без изменений ----------

@Composable
private fun GraphSettingsDialog(onDismiss: () -> Unit) {
    var maxDepth by remember { mutableStateOf(GraphSettings.maxDepth.toFloat()) }
    var curvatureThreshold by remember { mutableStateOf(GraphSettings.curvatureThreshold.toFloat()) }
    var relativeThreshold by remember { mutableStateOf(GraphSettings.relativeThreshold.toFloat()) }
    var useScaleSensitivity by remember { mutableStateOf(GraphSettings.useScaleSensitivity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                GraphSettings.maxDepth = maxDepth.toInt()
                GraphSettings.curvatureThreshold = curvatureThreshold.toDouble()
                GraphSettings.relativeThreshold = relativeThreshold.toDouble()
                GraphSettings.useScaleSensitivity = useScaleSensitivity
                onDismiss()
            }) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        title = { Text("Настройки графика") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Глубина адаптации", style = MaterialTheme.typography.bodyMedium)
                        Text(maxDepth.toInt().toString(), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = maxDepth, onValueChange = { maxDepth = it }, valueRange = 1f..20f, steps = 18)
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Порог кривизны", style = MaterialTheme.typography.bodyMedium)
                        Text("%.3f".format(curvatureThreshold), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = curvatureThreshold, onValueChange = { curvatureThreshold = it }, valueRange = 0.001f..0.5f)
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Относительный порог", style = MaterialTheme.typography.bodyMedium)
                        Text("%.3f".format(relativeThreshold), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = relativeThreshold, onValueChange = { relativeThreshold = it }, valueRange = 0.01f..1f)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Учитывать масштаб", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = useScaleSensitivity, onCheckedChange = { useScaleSensitivity = it })
                }
                TextButton(
                    onClick = {
                        maxDepth = 10f
                        curvatureThreshold = 0.05f
                        relativeThreshold = 0.1f
                        useScaleSensitivity = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Сбросить по умолчанию", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

private fun computePoints(
    expression: String,
    xMin: Double,
    xMax: Double,
    steps: Int,
    isVertical: Boolean
): List<Pair<Double, Double?>> {
    val result = mutableListOf<Pair<Double, Double?>>()
    val step = (xMax - xMin) / steps
    var currentVal = xMin

    while (currentVal <= xMax) {
        val y = MathEvaluator.evaluate(expression, currentVal)
        val res: Double? = if (y.isNaN() || y.isInfinite()) null else y

        if (isVertical) {
            result.add(res?.let { it to currentVal } ?: (0.0 to null))
        } else {
            result.add(currentVal to res)
        }
        currentVal += step
    }
    return result
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

private fun DrawScope.drawGrid(
    xMin: Double,
    xMax: Double,
    yOffset: Double,
    textMeasurer: TextMeasurer
) {
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
            val label = if (abs(gx - gx.roundToInt()) < 0.001)
                gx.roundToInt().toString()
            else
                gx.toBigDecimal().stripTrailingZeros().toPlainString()
            val measured = textMeasurer.measure(
                AnnotatedString(label),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
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
            val label = if (abs(gy - gy.roundToInt()) < 0.001)
                gy.roundToInt().toString()
            else
                gy.toBigDecimal().stripTrailingZeros().toPlainString()
            val measured = textMeasurer.measure(
                AnnotatedString(label),
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
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

private fun DrawScope.drawGraph(
    points: List<Pair<Double, Double?>>,
    xMin: Double,
    xMax: Double,
    yOffset: Double
) {
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
            y < yMin - yRange * 5 || y > yMax + yRange * 5
        ) {
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