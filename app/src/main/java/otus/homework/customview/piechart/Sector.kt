package otus.homework.customview.piechart

import android.graphics.Paint

data class Sector(
    val id: String,
    val startAngle: Float,
    val sweepAngle: Float,
    val paint: Paint
)