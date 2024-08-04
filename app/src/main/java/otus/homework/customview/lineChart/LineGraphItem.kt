package otus.homework.customview.lineChart

import android.graphics.PointF

data class LineGraphItem(
    val points: List<PointF>,
    val color: Int
)