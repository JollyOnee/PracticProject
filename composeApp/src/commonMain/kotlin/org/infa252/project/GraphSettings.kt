package org.infa252.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Глобальные настройки движка графиков.
 */
object GraphSettings {
    /**
     * Глубина вычислений (максимальная глубина рекурсии адаптивного шага).
     * Значение от 1 до 15.
     */
    var maxDepth by mutableIntStateOf(10)

    /**
     * Порог изгиба (абсолютный).
     * Чем меньше, тем детальнее график на изгибах.
     */
    var curvatureThreshold by mutableDoubleStateOf(0.05)

    /**
     * Относительный порог для малых значений (порог для 0).
     * Защищает от чрезмерной детализации около оси X.
     */
    var relativeThreshold by mutableDoubleStateOf(0.1)

    /**
     * Использовать адаптивную чувствительность к масштабу.
     * Если true, детализация не будет падать при отдалении.
     */
    var useScaleSensitivity by mutableStateOf(true)
}
