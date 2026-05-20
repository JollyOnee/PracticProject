package org.infa252.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Инициализация твоей основной логики
            val mathViewModel: MathViewModel = viewModel { MathViewModel(MathRepository()) }

            // Базовый экран приложения без интеграции камеры
            AppTheme {
                App(
                    viewModel = mathViewModel
                )
            }
        }
    }
}