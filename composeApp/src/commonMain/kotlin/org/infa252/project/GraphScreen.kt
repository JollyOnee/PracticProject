package org.infa252.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import org.infa252.project.MathViewModel
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
    val nativeLib = remember { NativeLib() }
    val graphEngine = remember { GraphEngine(nativeLib) }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showSettings by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
        graphEngine.computePoints(viewModel.formula, xMin, xMax)
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
                            "Settings",
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
                Text(
                    text = "Точек: ${points.size} • Шаг: ${formatDouble((xMax - xMin) / 120.0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
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

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Настройки движка") },
                text = {
                    Column {
                        Text("Глубина вычислений: ${GraphSettings.maxDepth}")
                        Slider(
                            value = GraphSettings.maxDepth.toFloat(),
                            onValueChange = { GraphSettings.maxDepth = it.toInt() },
                            valueRange = 1f..20f,
                            steps = 20
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Абсолютный порог: ${formatDouble(GraphSettings.curvatureThreshold)}")
                        Slider(
                            value = GraphSettings.curvatureThreshold.toFloat(),
                            onValueChange = { GraphSettings.curvatureThreshold = it.toDouble() },
                            valueRange = 0.001f..0.5f,
                            steps = 20

                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Порог для 0 (относ.): ${formatDouble(GraphSettings.relativeThreshold)}")
                        Slider(
                            value = GraphSettings.relativeThreshold.toFloat(),
                            onValueChange = { GraphSettings.relativeThreshold = it.toDouble() },
                            valueRange = 0.0001f..0.5f,
                            steps = 20
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Авто-детализация (зум):", modifier = Modifier.weight(1f))
                            Switch(
                                checked = GraphSettings.useScaleSensitivity,
                                onCheckedChange = { GraphSettings.useScaleSensitivity = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text("ОК")
                    }
                }
            )
        }

        if (showExportDialog) {
            var exportXMin by remember { mutableStateOf(xMin.toString()) }
            var exportXMax by remember { mutableStateOf(xMax.toString()) }
            val yRange = (xMax - xMin)
            var exportYMin by remember { mutableStateOf((-yRange/2 + yOffset).toString()) }
            var exportYMax by remember { mutableStateOf((yRange/2 + yOffset).toString()) }

            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Экспорт в SVG (HQ)") },
                text = {
                    Column {
                        Text("Настройте область для экспорта:", style = MaterialTheme.typography.labelMedium)
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
                                graphEngine.generateSvgContent(
                                    viewModel.formula, x1, x2, y1, y2
                                )
                            }
                            
                            // Вызываем платформенно-зависимое сохранение
                            saveSvg("graph_${viewModel.formula.replace("\\", "")}.svg", svg)

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
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Генерация HQ графика...", color = Color.White)
                }
            }
        }
    }
}

// Вспомогательная функция для форматирования чисел без String.format (для KMP)
private fun formatDouble(value: Double): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return "Infinity"
    
    // Для очень маленьких значений (пороги в настройках) увеличиваем точность
    return if (abs(value) < 1.0 && value != 0.0) {
        val rounded = (value * 1000).roundToInt() / 1000.0
        rounded.toString()
    } else {
        val rounded = (value * 10).roundToInt() / 10.0
        if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
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

private fun DrawScope.drawGraph(points: KmpTreeMap<Double, Double>, xMin: Double, xMax: Double, yOffset: Double) {
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

    for (entry in points) {
        val x = entry.key
        val y = entry.value
        if (y.isNaN() || y.isInfinite() ||
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
