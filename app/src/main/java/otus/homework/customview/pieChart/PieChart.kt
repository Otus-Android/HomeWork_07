package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.min

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var viewSize: Float = 0f
    private var strokeWidth: Float = 0f

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

        val w = 1000
        val h = 1000

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        val viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        viewSize = min(viewWidth, viewHeight).toFloat()
        setChartPartWidth(viewSize / 5)

        setMeasuredDimension(viewSize.toInt(), viewSize.toInt())
    }

    private fun setChartPartWidth(width: Float) {
        strokeWidth = width
        paint.strokeWidth = strokeWidth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val center = viewSize / 2

        paint.color = Color.BLACK
        paint.strokeWidth = 5f

        // рисуем линии
        canvas.drawLine(center, 0f, center, viewSize, paint)
        canvas.drawLine(0f, center, viewSize, center, paint)

        paint.strokeWidth = strokeWidth
        chartParts.forEach { it.draw(canvas, paint, viewSize) }
    }

}