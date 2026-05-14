package org.infa252.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig
import java.nio.ByteBuffer

@Composable
fun CameraCaptureScreen(
    onPhotoCaptured: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    var isProcessing by remember { mutableStateOf(false) }
    var latexResult by remember { mutableStateOf("") }

    // размеры рамки и позиция для обрезки
    var frameOffset by remember { mutableStateOf(IntOffset.Zero) }
    var frameSize by remember { mutableStateOf(IntSize.Zero) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { screenSize = it.size }
    ) {
        // превью камеры
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                }
            }
        )

        // затемнение вокруг рамки (4 полосы)
        if (frameSize != IntSize.Zero) {
            val overlayColor = Color.Black.copy(alpha = 0.5f)
            // верх
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { frameOffset.y.toDp() })
                    .align(Alignment.TopStart)
            ) { Surface(modifier = Modifier.fillMaxSize(), color = overlayColor) {} }
            // низ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { (screenSize.height - frameOffset.y - frameSize.height).toDp() })
                    .align(Alignment.BottomStart)
            ) { Surface(modifier = Modifier.fillMaxSize(), color = overlayColor) {} }
            // лево
            Box(
                modifier = Modifier
                    .width(with(density) { frameOffset.x.toDp() })
                    .height(with(density) { frameSize.height.toDp() })
                    .offset(y = with(density) { frameOffset.y.toDp() })
                    .align(Alignment.TopStart)
            ) { Surface(modifier = Modifier.fillMaxSize(), color = overlayColor) {} }
            // право
            Box(
                modifier = Modifier
                    .width(with(density) { (screenSize.width - frameOffset.x - frameSize.width).toDp() })
                    .height(with(density) { frameSize.height.toDp() })
                    .offset(y = with(density) { frameOffset.y.toDp() })
                    .align(Alignment.TopEnd)
            ) { Surface(modifier = Modifier.fillMaxSize(), color = overlayColor) {} }
        }

        // рамка прицела
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(160.dp)
                .align(Alignment.Center)
                .onGloballyPositioned { coords ->
                    frameOffset = IntOffset(
                        coords.localToRoot(androidx.compose.ui.geometry.Offset.Zero).x.toInt(),
                        coords.localToRoot(androidx.compose.ui.geometry.Offset.Zero).y.toInt()
                    )
                    frameSize = coords.size
                }
                .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        // кнопка назад
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // подсказка сверху
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "Наведи камеру на пример",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // результат LaTeX под рамкой
        if (latexResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center)
                    .padding(top = 180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Распознано:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Latex(
                        latex = latexResult,
                        config = LatexConfig(
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // кнопка съёмки / индикатор загрузки
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = {
                        isProcessing = true
                        latexResult = ""
                        val executor = ContextCompat.getMainExecutor(context)
                        cameraController.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val fullBitmap = image.toBitmap()
                                    image.close()

                                    // обрезаем по рамке
                                    val scaleX = fullBitmap.width.toFloat() / screenSize.width
                                    val scaleY = fullBitmap.height.toFloat() / screenSize.height
                                    val cropX = (frameOffset.x * scaleX).toInt().coerceAtLeast(0)
                                    val cropY = (frameOffset.y * scaleY).toInt().coerceAtLeast(0)
                                    val cropW = (frameSize.width * scaleX).toInt()
                                        .coerceAtMost(fullBitmap.width - cropX)
                                    val cropH = (frameSize.height * scaleY).toInt()
                                        .coerceAtMost(fullBitmap.height - cropY)

                                    val cropped = Bitmap.createBitmap(
                                        fullBitmap, cropX, cropY, cropW, cropH
                                    )
                                    onPhotoCaptured(cropped)
                                    isProcessing = false
                                }
                            }
                        )
                    },
                    modifier = Modifier.width(200.dp).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Решить пример",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val buffer: ByteBuffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}