package otus.homework.customview.pieChart

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class ChartPart(
    val name: String,
    percent: Float,
    private val color: Int
) {

    companion object {
        private const val TAG = "CHART_PART_TAG"
    }

    var startAngle: Float = 0f
    var sweepAngle: Float = 360 * percent

    // отступ между частями графика
    private val chartPartsMargin = 3f

    private val angleStartValue = 0f
    private val angleEndValue = chartPartsMargin * 3
    private var angleAnimValue = angleStartValue

    // центр родителя
    private var cX = 0
    private var cY = 0

    // размер родителя
    private var parentViewSize = 0f
    private var halfViewSize = 0f

    private var strokeWidth = 0f
    private var halfStrokeWidth: Float = 0f

    // пределы в которых рисуется график
    private var left: Float = 0f
    private var right: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f

    // ключ указываюший выбран график или нет
    private var isSelected = false

    private val strokeStartAnimValue = 0.2f
    private val strokeEndAnimValue = 0.25f
    private var strokeAnimValue = strokeStartAnimValue

    private var valueAnimator: ValueAnimator? = null

    /** Функция простановки размеров графика*/
    fun setViewSize(w: Int, h: Int) {
        cX = w / 2
        cY = h / 2

        parentViewSize = min(w, h).toFloat()
        halfViewSize = parentViewSize / 2

        strokeWidth = parentViewSize * strokeAnimValue
        halfStrokeWidth = strokeWidth * 0.5f
    }

    /** функция отрисовки части графика */
    fun draw(canvas: Canvas, paint: Paint) {

        paint.strokeWidth = strokeWidth

        left = cX - halfViewSize + halfStrokeWidth
        right = cX + halfViewSize - halfStrokeWidth
        top = cY - halfViewSize + halfStrokeWidth
        bottom = cY + halfViewSize - halfStrokeWidth

        paint.color = color

        val oval = RectF(left, top, right, bottom)
        val startAngle = startAngle + chartPartsMargin - angleAnimValue
        val sweepAngle = sweepAngle - chartPartsMargin + (2 * angleAnimValue)

        canvas.drawArc(oval, startAngle, sweepAngle, false, paint)
    }

    /** функция которая проверяет входит ли угол клика в диапозон углов этой части графика */
    fun isChartPartTap(tapAngle: Double): Boolean {
        val endAngle = startAngle + sweepAngle - chartPartsMargin
        return (startAngle - chartPartsMargin) <= tapAngle && tapAngle <= endAngle
    }

    /** функция которая проверит кликнул или пользователь на эту часть */
    fun isTouchOnChart(event: MotionEvent?): Boolean {
        return if (event == null) {
            false
        } else {

            val xTouch = event.x
            val yTouch = event.y

            val distanceToCenter = sqrt(
                (xTouch - cX).toDouble().pow(2.0) + (yTouch - cY).toDouble().pow(2.0)
            )

            val bigRadius = (right - left) / 2 + halfStrokeWidth
            val smallRadius = bigRadius - strokeWidth


            if (valueAnimator?.isRunning != true) {
                val isSectorClick = distanceToCenter in smallRadius..bigRadius
                if (isSectorClick) {
                    isSelected = !isSelected
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    fun animate(callback: () -> Unit) {

        val startStrokeValue = if (isSelected) strokeStartAnimValue else strokeEndAnimValue
        val endStrokeValue = if (isSelected) strokeEndAnimValue else strokeStartAnimValue
        val strokeKey = "stroke"
        val strokeHolder = PropertyValuesHolder.ofFloat(strokeKey, startStrokeValue, endStrokeValue)

        val startAngleValue = if (isSelected) angleStartValue else angleEndValue
        val endAngleValue = if (isSelected) angleEndValue else angleStartValue
        val angleKey = "angle"
        val angleHolder = PropertyValuesHolder.ofFloat(angleKey, startAngleValue, endAngleValue)

        if (valueAnimator?.isRunning != true) {
            valueAnimator = ValueAnimator.ofPropertyValuesHolder(strokeHolder, angleHolder).apply {
                interpolator = LinearInterpolator()
                duration = 500
                addUpdateListener {
                    angleAnimValue = it.getAnimatedValue(angleKey) as Float
                    strokeAnimValue = it.getAnimatedValue(strokeKey) as Float
                    callback.invoke()
                }
            }
            valueAnimator?.start()
        }
    }
}

