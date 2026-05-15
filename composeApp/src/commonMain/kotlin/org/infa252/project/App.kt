package org.infa252.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(
    viewModel: MathViewModel,
    onOpenCamera: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf("start") }

    AppTheme {
        when (currentScreen) {
            "start" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "MathSolver",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                        Button(
                            onClick = { currentScreen = "input" },
                            modifier = Modifier.width(200.dp).height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Начать",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            "input" -> {
                MathInputScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "start" },
                    onShowGraph = { currentScreen = "graph" },
                    onOpenCamera = onOpenCamera
                )
            }
            "graph" -> {
                GraphScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "input" }
                )
            }
        }
    }
}