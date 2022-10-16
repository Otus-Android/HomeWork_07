package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.atan2
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "PIE_CHART_TAG"
    }

    // событие касания
    private var motionEvent: MotionEvent? = null

    // угол касания, для определения сектора
    private var tapAngle = 0.0

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
    }

    private val chartParts = mutableListOf<ChartPart>()

    /** Метод установки значений из json*/
    fun drawChartParts(data: List<ChartPart>) {
        chartParts.clear()
        chartParts.addAll(data)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (MotionEvent.ACTION_DOWN == event.action) {
            motionEvent = event
            tapAngle = convertTouchEventToAngle(event)
            performClick()
            true
        } else {
            false
        }
    }

    private fun convertTouchEventToAngle(event: MotionEvent): Double {
        val x: Float = event.x - width * 0.5f
        val y: Float = event.y - height * 0.5f

        var angle = Math.toDegrees(atan2(y, x) + Math.PI / 2) - 90
        angle = if (angle < 0) angle + 360 else angle

        return angle
    }

    override fun performClick(): Boolean {
        super.performClick()

        chartParts.firstOrNull { it.isChartPartTap(tapAngle) }?.let { chartPart ->
            if (chartPart.isTouchOnChart(motionEvent)) {
                Toast.makeText(context, chartPart.name, Toast.LENGTH_SHORT).show()
                chartPart.animate { invalidate() }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cX = width / 2
        val cY = height / 2
        paint.strokeWidth = 2f

        canvas.drawLine(cX.toFloat(), 0f, cX.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0f, cY.toFloat(), width.toFloat(), cY.toFloat(), paint)


        var startAngle = 0f
        chartParts.forEach {
            it.startAngle = startAngle
            startAngle += it.sweepAngle

            it.setViewSize(width, height)
            it.draw(canvas, paint)
        }
    }

}