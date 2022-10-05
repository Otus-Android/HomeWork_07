package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ChartPart(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
) {

    var startAngle: Float = 0f
    var sweepAngle: Float = 0f
    var color: Int = Color.TRANSPARENT

    fun draw(canvas: Canvas, paint: Paint, parentSize: Float) {

        val strokeWidth = paint.strokeWidth
        val halfWidth = strokeWidth / 2

        val left: Float = 0f + halfWidth
        val right: Float = parentSize - halfWidth
        val top: Float = 0f + halfWidth
        val bottom: Float = parentSize - halfWidth

        paint.color = color
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, paint)
    }

}