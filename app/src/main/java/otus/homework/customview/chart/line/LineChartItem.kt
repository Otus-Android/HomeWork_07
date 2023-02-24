package otus.homework.customview.chart.line

import android.graphics.PointF

data class LineChartItem(
    val categoryName: String,
    val points: List<PointF>,
    val color: Int
)