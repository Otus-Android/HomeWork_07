package otus.homework.customview

import android.graphics.Paint
import android.graphics.PointF

data class PieSlice(
    val category: String,
    var value: Double,
    var startAngle: Float,
    var rotationAngle: Float,
    var indicatorCircleLocation: PointF,
    val paint: Paint,
    var state: PieState = PieState.MINIMIZED
)
