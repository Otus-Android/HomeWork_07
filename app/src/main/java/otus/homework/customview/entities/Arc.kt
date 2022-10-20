package otus.homework.customview.entities

import android.graphics.Paint

data class Arc(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val paint: Paint,
    val categoryId: Int
)
