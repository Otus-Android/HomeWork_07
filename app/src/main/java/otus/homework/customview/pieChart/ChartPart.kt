package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ChartPart(
    private val startAngle: Float,
    private val sweepAngle: Float,
    private val color: Int
) {

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