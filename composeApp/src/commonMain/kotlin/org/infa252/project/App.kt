package org.infa252.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {

    var currentScreen by remember { mutableStateOf("start") }


    val darkGreenBg = Color(0xFF161D15)
    val lightGreenBg = Color(0xFFF4FCED)
    val accentGreen = Color(0xFF006E1C)

    MaterialTheme {
        when (currentScreen) {
            "start" -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(lightGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "MathSolver",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = darkGreenBg,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Кнопка начать
                        Button(
                            onClick = { currentScreen = "input" },
                            modifier = Modifier
                                .width(200.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                        ) {
                            Text(
                                text = "Начать",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            "input" -> {
                MathInputScreen(
                    onBack = { currentScreen = "start" },
                    onSolve = { formula ->

                        println("Formula to solve: $formula")
                    }
                )
            }


        }
    }
}