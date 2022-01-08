package otus.homework.customview.pie_chart

import android.graphics.Paint
import android.graphics.PointF


data class PieSlice(
    val name: String,
    var value: Int,
    val paint: Paint
) {
    var startAngle = 0F
    var sweepAngle = 0F
    val indicatorCircleLocation = PointF()
}