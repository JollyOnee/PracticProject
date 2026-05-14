package org.infa252.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathInputScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit,
    onShowGraph: () -> Unit
){
    val hScrollState = rememberScrollState()
    val mainScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Визуализация формулы с курсором
    val visualFormula = remember(viewModel.formula, viewModel.cursorIndex) {
        val safeIndex = viewModel.cursorIndex.coerceIn(0, viewModel.formula.length)
        if (viewModel.formula.isEmpty()) "|"
        else StringBuilder(viewModel.formula).insert(safeIndex, "|").toString()
    }

    // Динамический шрифт
    val dynamicFontSize = remember(viewModel.formula.length) {
        when {
            viewModel.formula.length < 15 -> 36.sp
            viewModel.formula.length < 45 -> 28.sp
            else -> 20.sp
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val isCtrl = keyEvent.isCtrlPressed
                    when {
                        isCtrl && keyEvent.key == Key.Z -> {
                            viewModel.undo()
                            true
                        }
                        isCtrl && keyEvent.key == Key.Y -> {
                            viewModel.redo()
                            true
                        }
                        keyEvent.key == Key.DirectionLeft -> {
                            viewModel.moveCursorLeft()
                            true
                        }
                        keyEvent.key == Key.DirectionRight -> {
                            viewModel.moveCursorRight()
                            true
                        }
                        keyEvent.key == Key.Backspace -> {
                            viewModel.onDelete()
                            true
                        }
                        keyEvent.key == Key.Enter -> {
                            viewModel.solveFormula()
                            true
                        }
                        keyEvent.key == Key.Escape -> {
                            onBack()
                            true
                        }
                        keyEvent.key == Key.Tab -> {
                            // Logic for Tab jump
                            viewModel.moveCursorRight() // Currently our smart right arrow handles jumps
                            true
                        }
                        else -> {
                            val char = keyEvent.utf16CodePoint.toChar()
                            val symbol = when (char) {
                                '%' -> "\\frac{}{}"
                                '*' -> "\\times"
                                '/' -> "÷"
                                '^' -> "^{}"
                                '_' -> "_{}"
                                '(' -> "\\left( \\right)"
                                '[' -> "\\left[ \\right]"
                                else -> char.toString()
                            }
                            if (char.isDigit() || char in "+-*/.(),=x%^_{}[]") {
                                viewModel.onSymbolClick(symbol)
                                true
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    false
                }
            },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MathSolver", 
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.widthIn(max = 600.dp).padding(bottom = 12.dp, top = 8.dp)) {
                    // Вкладки
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), 
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("±", "f(x)", "sin").forEach { id ->
                            val isSelected = viewModel.currentTab == id
                            Button(
                                onClick = { viewModel.currentTab = id },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                                )
                            ) { 
                                Text(
                                    id,
                                    style = MaterialTheme.typography.labelLarge
                                ) 
                            }
                        }
                    }

                    // Навигация
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControlKey("←", modifier = Modifier.weight(1f)) { viewModel.moveCursorLeft() }
                        ControlKey("→", modifier = Modifier.weight(1f)) { viewModel.moveCursorRight() }
                        ControlKey("⌫", modifier = Modifier.weight(1f)) { viewModel.onDelete() }
                        ControlKey("C", modifier = Modifier.weight(1f)) { viewModel.onClear() }
                        ControlKey("=", modifier = Modifier.weight(1f), isAccent = true) { viewModel.solveFormula() }
                    }

                    MathKeyboard(tab = viewModel.currentTab, onSymbolClick = { viewModel.onSymbolClick(it) })

                    Button(
                        onClick = {
                            viewModel.solveFormula()
                            onShowGraph()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { 
                        Text(
                            "показать график",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        ) 
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(mainScrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(hScrollState), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Latex(
                        latex = visualFormula, 
                        config = LatexConfig(
                            fontSize = dynamicFontSize, 
                            color = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
            // Отображение сырого Latex для отладки
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(top = 10.dp), 
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant)
            ) {
                val clipboardManager = LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Latex: ${viewModel.formula}",
                        modifier = Modifier.weight(1f).padding(vertical = 10.dp), 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { 
                        clipboardManager.setText(AnnotatedString(viewModel.formula))
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Latex",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            // Поле результата
            AppTheme {
                Box(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(top = 16.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "= ",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            viewModel.result.ifEmpty { "0" },
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.inverseSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlKey(label: String, modifier: Modifier = Modifier, isAccent: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    ) {
        Text(
            text = label, 
            modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = if (isAccent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MathKeyboard(tab: String, onSymbolClick: (String) -> Unit) {
    val symbols = remember(tab) {
        when (tab) {
            "±" -> listOf(
                "7" to "7", "8" to "8", "9" to "9", "÷" to "÷",
                "4" to "4", "5" to "5", "6" to "6", "×" to "\\times",
                "1" to "1", "2" to "2", "3" to "3", "-" to "-",
                "0" to "0", "." to ".", "π" to "\\pi", "+" to "+",
                "□/□" to "\\frac{}{}", "√□" to "\\sqrt{}", "□²" to "^{2}", "( )" to "\\left( \\right)",
                "x" to "x", ">" to ">", "%" to "\\frac{}{}", "=" to "="
            )
            "f(x)" -> listOf(
                "|x|" to "\\left| {} \\right|", "log₁₀" to "\\log_{10}{}", "A" to "A_{}^{}", "e" to "e",
                "log₂" to "\\log_{2}{}", "P" to "P_{}^{}", "!" to "!", "logₙ" to "\\log_{}{}",
                "C" to "C_{}^{}", "ln" to "\\ln{}", "Σ" to "\\sum_{}^{}{}", "∫" to "\\int_{}^{}{}dx",
                "lim" to "\\lim_{ \\to }{}"
            )
            "sin" -> listOf(
                "sin" to "\\sin{}", "cos" to "\\cos{}", "tan" to "\\tan{}", "asin" to "\\arcsin{}", "acos" to "\\arccos{}", "atan" to "\\arctan{}",
                "sinh" to "\\sinh{}", "cosh" to "\\cosh{}", "tanh" to "\\tanh{}", "rad" to "\\text{rad}", "!" to "!"
            )
            else -> emptyList()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        userScrollEnabled = false
    ) {
        items(symbols) { (label, value) ->
            Surface(
                onClick = { onSymbolClick(value) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
