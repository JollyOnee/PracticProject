package org.infa252.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.model.LatexConfig

// --- ЦВЕТОВАЯ ПАЛИТРА (Добавлена сюда, чтобы не было ошибок) ---
val darkGreenBg = Color(0xFF161D15)
val lightGreenBg = Color(0xFFF4FCED)
val accentGreen = Color(0xFF006E1C)
val secondaryText = Color(0xFF6D7B69)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathInputScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val hScrollState = rememberScrollState()
    val mainScrollState = rememberScrollState()

    // Визуализация формулы с курсором
    val visualFormula = remember(viewModel.formula, viewModel.cursorIndex) {
        val safeIndex = viewModel.cursorIndex.coerceIn(0, viewModel.formula.length)
        val withCursor = if (viewModel.formula.isEmpty()) "|"
        else StringBuilder(viewModel.formula).insert(safeIndex, "|").toString()
        withCursor.replace("%", "\\%")
    }

    // Динамический шрифт
    val dynamicFontSize = when {
        viewModel.formula.length < 15 -> 36.sp
        viewModel.formula.length < 45 -> 28.sp
        else -> 20.sp
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = lightGreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MathSolver", fontWeight = FontWeight.Bold, color = darkGreenBg) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = darkGreenBg)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFDDE5D7),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.widthIn(max = 600.dp).padding(bottom = 12.dp, top = 8.dp)) {
                    // Вкладки
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("±", "f(x)", "sin").forEach { id ->
                            val isSelected = viewModel.currentTab == id
                            Button(
                                onClick = { viewModel.currentTab = id },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) darkGreenBg else Color.White,
                                    contentColor = if (isSelected) Color.White else darkGreenBg
                                )
                            ) { Text(id, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }

                    // Навигация
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ControlKey("←") { viewModel.moveCursorLeft() }
                        ControlKey("→") { viewModel.moveCursorRight() }
                        ControlKey("⌫") { viewModel.onDelete() }
                        ControlKey("C") { viewModel.onClear() }
                        ControlKey("=", isAccent = true) { viewModel.solveFormula() }
                    }

                    MathKeyboard(tab = viewModel.currentTab, onSymbolClick = { viewModel.onSymbolClick(it) })

                    Button(
                        onClick = { viewModel.solveFormula() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                    ) { Text("показать шаги решения", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp).verticalScroll(mainScrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).height(140.dp).background(darkGreenBg, RoundedCornerShape(24.dp)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(hScrollState), verticalAlignment = Alignment.CenterVertically) {
                    Latex(latex = visualFormula, config = LatexConfig(fontSize = dynamicFontSize, color = Color.White))
                }
            }
            // Отображение сырого Latex для отладки
            Card(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(top = 10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0E2))) {
                Text(text = "Latex: ${viewModel.formula}", modifier = Modifier.padding(10.dp), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = darkGreenBg)
            }
            // Поле результата
            Box(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).padding(top = 16.dp, end = 8.dp), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("= ", fontSize = 26.sp, color = secondaryText)
                    Text(viewModel.result.ifEmpty { "0" }, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = darkGreenBg)
                }
            }
        }
    }
}

@Composable
fun ControlKey(label: String, isAccent: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(44.dp).width(64.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isAccent) accentGreen else Color(0xFFBCCBB6)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, color = if (isAccent) Color.White else darkGreenBg, fontSize = 20.sp)
        }
    }
}

// Функцию MathKeyboard тоже нужно оставить в этом файле, если она не вынесена
@Composable
fun MathKeyboard(tab: String, onSymbolClick: (String) -> Unit) {
    val symbols = when (tab) {
        "±" -> listOf(
            "7" to "7", "8" to "8", "9" to "9", "÷" to "/",
            "4" to "4", "5" to "5", "6" to "6", "×" to "*",
            "1" to "1", "2" to "2", "3" to "3", "-" to "-",
            "0" to "0", "," to ".", "π" to "\\pi", "+" to "+",
            "□/□" to "\\frac{}{}", "√□" to "\\sqrt{}", "□²" to "^{2}", "( )" to "( )",
            "x" to "x", ">" to ">", "%" to "%", "=" to "="
        )
        "f(x)" -> listOf(
            "|x|" to "| |", "log₁₀" to "\\log_{10}{}", "A" to "A_{}^{}", "e" to "e",
            "log₂" to "\\log_{2}{}", "P" to "P_{}^{}", "!" to "!", "logₙ" to "\\log_{}{}",
            "C" to "C_{}^{}", "ln" to "\\ln{}", "d/dx" to "\\frac{d}{dx}{}", "∫dx" to "\\int{}dx"
        )
        "sin" -> listOf(
            "sin" to "\\sin{}", "cos" to "\\cos{}", "tan" to "\\tan{}", "asin" to "\\arcsin{}", "acos" to "\\arccos{}", "atan" to "\\arctan{}",
            "sinh" to "\\sinh{}", "cosh" to "\\cosh{}", "tanh" to "\\tanh{}", "rad" to "rad", "°" to "^{\\circ}", "!" to "!"
        )
        else -> emptyList()
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
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkGreenBg)
                }
            }
        }
    }
}