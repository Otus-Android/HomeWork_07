package otus.homework.customview.pieChart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import kotlin.math.pow
import kotlin.math.sqrt

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

    private var parentViewSize = 0f
    private var halfViewSize = 0f

    private var strokeWidth = 0f
    private var halfStrokeWidth: Float = 0f

    private var strokePadding: Float = 0f

    private var valueAnimator: ValueAnimator? = null

    private var width: Int = 0
    private var height: Int = 0

    private var chartPadding = 0f


    private var left: Float = 0f
    private var right: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f

    fun setCenterCoordinates(x: Int, y: Int) {
        cX = x
        cY = y
    }

    fun setParentViewSize(parentSize: Float, w: Int, h: Int) {
        width = w
        height = h

        parentViewSize = parentSize
        halfViewSize = parentViewSize / 2

        strokeWidth = parentViewSize / 10
        halfStrokeWidth = strokeWidth / 2

        strokePadding = strokeWidth
    }

    fun draw(canvas: Canvas, paint: Paint, padding: Float = 0f) {

        chartPadding = padding

        left = cX - halfViewSize + strokePadding + chartPadding
        right = cX + halfViewSize - strokePadding - chartPadding
        top = cY - halfViewSize + strokePadding + chartPadding
        bottom = cY + halfViewSize - strokePadding - chartPadding

        paint.color = color

        Log.i(TAG, "draw: $strokePadding")

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

    fun isTouchOnChart(event: MotionEvent?): Boolean {
        return if (event == null) {
            false
        } else {

            val xTouch = event.x
            val yTouch = event.y

            val xCenter = width * 0.5f
            val yCenter = height * 0.5f

            val distanceToCenter = sqrt(
                (xTouch - xCenter).toDouble().pow(2.0) + (yTouch - yCenter).toDouble().pow(2.0)
            )

            val bigRadius = (right - left) / 2 - chartPadding
            val smallRadius = bigRadius - strokeWidth


            distanceToCenter in bigRadius..smallRadius
        }
    }

    fun animate(callback: () -> Unit) {
        val startValue =
            if (strokePadding.toInt() == halfStrokeWidth.toInt()) halfStrokeWidth else strokeWidth
        val endValue =
            if (strokePadding.toInt() == halfStrokeWidth.toInt()) strokeWidth else halfStrokeWidth

        val isRunning = valueAnimator?.isRunning ?: false
        if (!isRunning) {
            valueAnimator = ValueAnimator.ofFloat(startValue, endValue).apply {
                interpolator = LinearInterpolator()
                duration = 500
                addUpdateListener {
                    strokePadding = it.animatedValue as Float
                    callback.invoke()
                }
            }
            valueAnimator?.start()
        }
    }

}