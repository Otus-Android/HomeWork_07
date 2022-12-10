package otus.homework.customview.line_chart

import android.graphics.PointF

data class LineChartItem(
    val points: List<PointF>,
    val color: Int
)