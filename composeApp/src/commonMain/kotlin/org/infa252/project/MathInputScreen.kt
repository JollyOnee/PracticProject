package org.infa252.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathInputScreen(
    onBack: () -> Unit,
    onSolve: (String) -> Unit
) {
    var formula by remember { mutableStateOf("2+2") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Калькулятор",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onSolve(formula) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006E1C)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Показать решение",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFFFFFF)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFFFFFFF))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Formula Display & Input Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // We'll use BasicTextField for a clean look like in the image
                    Box(modifier = Modifier.weight(1f)) {
                        if (formula.isEmpty()) {
                            Text(
                                "Введите формулу",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        
                        // Rendered LaTeX or simple text if not yet valid LaTeX
                        // For the input, we show the actual typed text with a cursor-like feel
                        BasicTextField(
                            value = formula,
                            onValueChange = { formula = it },
                            textStyle = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (formula.isNotEmpty()) {
                        IconButton(onClick = { formula = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Очистить",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Dashed divider or simple line
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Result Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "= ",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "4", // Mocked result for now
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Rendered LaTeX preview if it's complex
            if (formula.contains("\\") || formula.contains("^") || formula.contains("_")) {
                Text("Предпросмотр:", style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Latex(
                        latex = formula,
                        config = LatexConfig(
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun MathInputScreenPreview() {
    AppTheme {
        MathInputScreen(onBack = {}, onSolve = {})
    }
}
