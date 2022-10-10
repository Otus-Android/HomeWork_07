package otus.homework.customview.pieChart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log

class ChartPart(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
) {


    companion object {
        private const val TAG = "CHART_PART_TAG"
    }

    var startAngle: Float = 0f
    var sweepAngle: Float = 0f
    var color: Int = Color.TRANSPARENT

    private var cX = 0
    private var cY = 0

    private var valueAnimator: ValueAnimator? = null

    fun setCenterCoordinates(x: Int, y: Int) {
        cX = x
        cY = y
    }

    fun draw(canvas: Canvas, paint: Paint, parentSize: Float, padding: Float = 0f) {

        val strokeWidth = paint.strokeWidth
        val halfStrokeWidth = strokeWidth / 2

        val halfViewSize = parentSize / 2

        val left: Float = cX - halfViewSize + halfStrokeWidth + padding
        val right: Float = cX + halfViewSize - halfStrokeWidth - padding
        val top: Float = cY - halfViewSize + halfStrokeWidth + padding
        val bottom: Float = cY + halfViewSize - halfStrokeWidth - padding

        paint.color = color
        val oval = RectF(left, top, right, bottom)
        canvas.drawArc(
            oval,
            startAngle - 0.5f,
            sweepAngle - 0.5f,
            false, paint
        )
    }

    fun isChartPartTap(tapAngle: Double): Boolean {
        val endAngle = startAngle + sweepAngle - 1.0
        return (startAngle - 0.5) <= tapAngle && tapAngle <= endAngle
    }


    fun animate(callback: () -> Unit) {
        valueAnimator = ValueAnimator.ofFloat(0f, 2000f).apply {
            duration = 4000
            callback.invoke()
        }
        valueAnimator?.start()
    }

}