package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
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

    // отступ между частями графика
    private val chartPartsMargin = 3f

    /** Функция простановки размеров графика*/
    fun setViewSize(w: Int, h: Int) {
        cX = w / 2
        cY = h / 2

        parentViewSize = min(w, h).toFloat()
        halfViewSize = parentViewSize / 2

        strokeWidth = parentViewSize * 0.2f
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
        canvas.drawArc(
            oval,
            startAngle - chartPartsMargin,
            sweepAngle - chartPartsMargin,
            false, paint
        )
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

            isSelected = distanceToCenter in smallRadius..bigRadius
            isSelected
        }
    }

    fun animate(callback: () -> Unit) {

        /*ValueAnimator.ofFloat(0f, 10F).apply {
            interpolator = LinearInterpolator()
            duration = 1000
            addUpdateListener {
                startAngle -= it.animatedValue as Float
                //sweepAngle += it.animatedValue as Float

                callback.invoke()
            }
        }.start()*/
    }

    /*val startValue =
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
    }*/
}

