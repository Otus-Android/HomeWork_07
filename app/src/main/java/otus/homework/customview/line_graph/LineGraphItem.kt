package otus.homework.customview.line_graph

import android.graphics.PointF

data class LineGraphItem(
    val points: List<PointF>,
    val color: Int
)