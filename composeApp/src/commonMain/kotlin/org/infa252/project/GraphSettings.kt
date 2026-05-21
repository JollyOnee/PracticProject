package org.infa252.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GraphSettings {
    var maxDepth by mutableStateOf(10)
    var curvatureThreshold by mutableStateOf(0.05)
    var relativeThreshold by mutableStateOf(0.1)
    var useScaleSensitivity by mutableStateOf(true)
}