package otus.homework.customview.presentation.pie

import otus.homework.customview.presentation.pie.chart.PieStyle
import otus.homework.customview.presentation.pie.chart.PieData

data class PieChartUiState(
    val data: PieData = PieData(emptyList()),
    val style: PieStyle = PieStyle.PIE
)
