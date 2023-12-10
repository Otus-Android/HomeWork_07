package otus.homework.customview.presentation.pie

import otus.homework.customview.presentation.pie.chart.PieData
import otus.homework.customview.presentation.pie.chart.PieStyle

/**
 * Состояние отображения кругового графика
 *
 * @param data данные кругового графика
 * @param isDebugEnabled признак включения отображения отладочной информации
 * @param style стиль отображения кругового графика
 */
data class PieChartUiState(
    val data: PieData = PieData(),
    val isDebugEnabled: Boolean = false,
    val style: PieStyle = PieStyle.PIE
)
