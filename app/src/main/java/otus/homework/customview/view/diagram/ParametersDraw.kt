package otus.homework.customview.view.diagram

import android.graphics.Paint
import android.graphics.RectF

data class ParametersDraw(
    val startAngle: Float,
    val sweepAngle: Float,
    val useCenter: Boolean,
    val paint: Paint,
    val percent:Float,
    val category:String
)