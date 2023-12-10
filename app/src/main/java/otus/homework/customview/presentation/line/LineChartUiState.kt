package otus.homework.customview.presentation.line

import otus.homework.customview.presentation.line.chart.LineData

data class LineChartUiState(
    val current: LineData? = null,
    val lines: List<LineData> = emptyList(),
    val isDebugEnabled: Boolean = false
)
