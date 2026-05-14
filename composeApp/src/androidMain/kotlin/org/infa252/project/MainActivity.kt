package org.infa252.project

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = MathRepository()
    private val viewModel = MathViewModel(repository)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showCameraScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showApp()
    }

    private fun showApp() {
        setContent {
            App(
                viewModel = viewModel,
                onOpenCamera = { openCameraWithPermission() }
            )
        }
    }

    private fun openCameraWithPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> showCameraScreen()

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showCameraScreen() {
        val geminiService = GeminiMathService()
        setContent {
            AppTheme {
                CameraCaptureScreen(
                    onPhotoCaptured = { bitmap: Bitmap ->
                        lifecycleScope.launch {
                            val latex = geminiService.recognizeExpression(bitmap)
                            viewModel.formula = latex
                            viewModel.cursorIndex = latex.length
                            showApp()
                        }
                    },
                    onBack = { showApp() }
                )
            }
        }
    }
}