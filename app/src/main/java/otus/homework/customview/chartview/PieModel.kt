package otus.homework.customview.chartview

import android.graphics.Paint
import android.graphics.PointF

data class PieModel(
    val name: String,
    var amount: Double,
    var startAngle: Float = 0f,
    var sweepAngle: Float = 0f,
    var categoryLocation: PointF = PointF(),
    val paint: Paint
)
