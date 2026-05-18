package org.infa252.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val mathViewModel = viewModel { MathViewModel(MathRepository()) }
            val cameraViewModel = remember { CameraViewModel(CameraRepository()) }
            var showCamera by remember { mutableStateOf(false) }
            var latexCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

            if (showCamera) {
                AppTheme {
                    CameraScreen(
                        viewModel = cameraViewModel,
                        onBack = {
                            cameraViewModel.reset()
                            showCamera = false
                        },
                        onLatexReady = { latex ->
                            latexCallback?.invoke(latex)
                            cameraViewModel.reset()
                            showCamera = false
                        }
                    )
                }
            } else {
                App(
                    viewModel = mathViewModel,
                    onOpenCamera = { callback ->
                        latexCallback = callback
                        showCamera = true
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val viewModel = viewModel { MathViewModel(MathRepository()) }
    App(viewModel = viewModel)
}