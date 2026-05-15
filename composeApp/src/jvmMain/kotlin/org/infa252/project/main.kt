package org.infa252.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PracticProject",
    ) {
        val viewModel = viewModel { MathViewModel(MathRepository()) }
        App(viewModel = viewModel)
    }
}