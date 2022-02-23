package alektas.views.line_chart

import androidx.annotation.ColorInt

data class LineChartDataSet(
    val id: Int,
    val label: String,
    val points: List<LineChartPoint>,
    @ColorInt val color: Int,
)
