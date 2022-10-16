package otus.homework.customview.pieChart

import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import java.time.format.TextStyle
import kotlin.math.*

class ChartPart(
    val name: String,
    private val percent: Float,
    private val color: Int
) {

    companion object {
        private const val TAG = "CHART_PART_TAG"
    }

    var startAngle: Float = 0f
    var sweepAngle: Float = 360 * percent

    // отступ между частями графика
    private val chartPartsMargin = 3f

    private var angleAnimValue = 0f

    // центр родителя
    private var cX = 0
    private var cY = 0

    // размер родителя
    private var parentViewSize = 0f
    private var halfViewSize = 0f

    private var strokeWidth = 0f
    private var halfStrokeWidth: Float = 0f

    // пределы в которых рисуется график
    private var bigRadius: Float = 0f
    private var smallRadius: Float = 0f

    // ключ указываюший выбран график или нет
    private var isSelected = false

    private var strokeAnimValue = 0.2f

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

        val left = cX - halfViewSize + halfStrokeWidth
        val right = cX + halfViewSize - halfStrokeWidth
        val top = cY - halfViewSize + halfStrokeWidth
        val bottom = cY + halfViewSize - halfStrokeWidth

        bigRadius = (right - left) / 2 + halfStrokeWidth
        smallRadius = bigRadius - strokeWidth

        paint.color = color

        val oval = RectF(left, top, right, bottom)
        val startAngle = startAngle + chartPartsMargin - angleAnimValue
        val sweepAngle = sweepAngle - chartPartsMargin + (2 * angleAnimValue)

        Log.i(TAG, "draw: $angleAnimValue")

        paint.style = Paint.Style.STROKE
        canvas.drawArc(oval, startAngle, sweepAngle, false, paint)

        drawText(canvas, paint)
    }

    private fun drawText(canvas: Canvas, paint: Paint) {
        val textSize = 40f

        val alpha = startAngle + sweepAngle / 2
        val d = bigRadius - ((bigRadius - smallRadius) / 2)
        val x = cX + d * cos(Math.toRadians(alpha.toDouble()))
        val y = cY + d * sin(Math.toRadians(alpha.toDouble())) + textSize / 2

        paint.style = Paint.Style.FILL
        paint.textSize = textSize
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        val text = "%.2f".format(percent * 100) + "%"
        canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
    }

    /** функция которая проверяет входит ли угол клика в диапозон углов этой части графика */
    fun chartTap(event: MotionEvent?): Boolean {
        return event?.let {
            val tapAngle = convertTouchEventToAngle(it)
            val endAngle = startAngle + sweepAngle - chartPartsMargin

            // проверить принадлежит ли угол данному сектору
            val isTapInThisAngleSector =
                (startAngle - chartPartsMargin) <= tapAngle && tapAngle <= endAngle
            return if (isTapInThisAngleSector) {
                isTouchOnChart(it)
            } else false
        } ?: false
    }

    /** Функция, которая определяет угол по координатам качания */
    private fun convertTouchEventToAngle(event: MotionEvent): Double {
        val x: Float = event.x - cX
        val y: Float = event.y - cY

        var angle = Math.toDegrees(atan2(y, x) + Math.PI / 2) - 90
        angle = if (angle < 0) angle + 360 else angle

        return angle
    }

    /** функция которая проверит кликнул или пользователь на визульную часть данного сектора */
    private fun isTouchOnChart(event: MotionEvent?): Boolean {
        return if (event == null) {
            false
        } else {

            val xTouch = event.x
            val yTouch = event.y

            val distanceToCenter = sqrt(
                (xTouch - cX).toDouble().pow(2.0) + (yTouch - cY).toDouble().pow(2.0)
            )

            distanceToCenter in smallRadius..bigRadius
        }
    }

    fun animate(animAngle: Float, animStroke: Float) {
        angleAnimValue = animAngle
        strokeAnimValue = animStroke
    }
}

