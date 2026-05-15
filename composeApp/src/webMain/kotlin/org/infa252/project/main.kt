package org.infa252.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        val viewModel = viewModel { MathViewModel(MathRepository()) }
        App(viewModel = viewModel)
    }
}