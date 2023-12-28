package otus.homework.customview.pojo

import android.graphics.Color
import kotlin.math.round


data class Sector(
    val startAngle: Float,
    val partPieDegree: Float,
    val color: Int,
) {
    val endAngle: Float get() = partPieDegree + startAngle
    val partPiePercentage: String get() = String.format("%.1f%%", partPieDegree / 3.6)
}