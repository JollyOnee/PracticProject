package org.infa252.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = viewModel { MathViewModel(MathRepository()) }
            App(
                viewModel = viewModel
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val viewModel = viewModel { MathViewModel(MathRepository()) }
    App(
        viewModel = viewModel
    )
}