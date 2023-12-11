package otus.homework.customview.presentation.line

import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.line.chart.LineData

/**
 * Состояние отображения линейного графика
 *
 * @param current текущие данные линейного графика
 * @param categories список категорий
 * @param isDebugEnabled признак включения отображения отладочной информации
 */
data class LineChartUiState(
    val current: LineData? = null,
    val categories: List<Category> = emptyList(),
    val isDebugEnabled: Boolean = false
)
