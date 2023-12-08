package otus.homework.customview.presentation.line

import otus.homework.customview.presentation.line.chart.LineData

data class LineChartUiState(
    val data: LineData = LineData(),
    val isDebugEnabled: Boolean = false
)
