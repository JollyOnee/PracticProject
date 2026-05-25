package org.infa252.project

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            val mathViewModel: MathViewModel = viewModel { MathViewModel(MathRepository()) }
            var showCamera by remember { mutableStateOf(false) }

            AppTheme {
                if (showCamera) {
                    CameraScreen(
                        onBack = { showCamera = false },
                        onLatexReceived = { latex ->
                            mathViewModel.applyLatex(latex)
                            showCamera = false
                        }
                    )
                } else {
                    App(
                        viewModel = mathViewModel,
                        onOpenCamera = { showCamera = true }
                    )
                }
            }
        }
    }
}