package org.infa252.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onLatexReceived: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Наведи камеру на формулу") }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Превью камеры
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        statusText = "Ошибка камеры: ${e.message}"
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Рамка для наведения на формулу
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 80.dp, vertical = 360.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 4.dp.toPx()
                val cornerLength = 40.dp.toPx()
                val color = Color.White
                val w = size.width
                val h = size.height

                // Верхний левый угол
                drawLine(color, Offset(0f, cornerLength), Offset(0f, 0f), strokeWidth)
                drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)

                // Верхний правый угол
                drawLine(color, Offset(w - cornerLength, 0f), Offset(w, 0f), strokeWidth)
                drawLine(color, Offset(w, 0f), Offset(w, cornerLength), strokeWidth)

                // Нижний левый угол
                drawLine(color, Offset(0f, h - cornerLength), Offset(0f, h), strokeWidth)
                drawLine(color, Offset(0f, h), Offset(cornerLength, h), strokeWidth)

                // Нижний правый угол
                drawLine(color, Offset(w - cornerLength, h), Offset(w, h), strokeWidth)
                drawLine(color, Offset(w, h - cornerLength), Offset(w, h), strokeWidth)
            }
        }

        // Верхняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
        }

        // Статус и кнопка снизу
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = statusText,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                IconButton(
                    onClick = {
                        val capture = imageCapture ?: return@IconButton
                        isLoading = true
                        statusText = "Распознаю формулу..."
                        capture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    image.close()
                                    scope.launch {
                                        val latex = GroqApiService.extractLatex(bitmap)
                                        isLoading = false
                                        if (latex.startsWith("Ошибка")) {
                                            statusText = latex
                                        } else {
                                            onLatexReceived(latex)
                                        }
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    isLoading = false
                                    statusText = "Ошибка съёмки: ${exception.message}"
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Сфотографировать",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}