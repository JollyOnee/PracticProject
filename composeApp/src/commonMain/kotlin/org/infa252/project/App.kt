package org.infa252.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import practicproject.composeapp.generated.resources.Res
import practicproject.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    AppTheme {
        var screen by remember { mutableStateOf("home") }

        when (screen) {
            "home" -> {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .safeContentPadding()
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "MathSolver",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(onClick = { screen = "input" }) {
                        Text("Перейти к калькулятору")
                    }
                }
            }
            "input" -> {
                MathInputScreen(
                    onBack = { screen = "home" },
                    onSolve = { formula ->
                        // Handle solve action
                    }
                )
            }
        }
    }
}
