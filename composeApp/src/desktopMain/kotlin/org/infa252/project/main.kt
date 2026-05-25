package org.infa252.project

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Репозиторий можно оставить снаружи, он не влияет на рекомпозицию
    val repository = MathRepository()

    Window(
        onCloseRequest = ::exitApplication,
        title = "PracticProject",
    ) {

        val viewModel = remember { MathViewModel(repository) }

        App(viewModel = viewModel)
    }
}