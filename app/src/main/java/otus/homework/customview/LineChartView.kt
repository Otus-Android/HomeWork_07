package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.text.StaticLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.max

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val offsetX = 40f.toPx
    private val offsetY = 20f.toPx

    private val bounds = RectF()
    private val tempRect = Rect()
    private var chartData: ChartData? = null

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        textSize = 14f.toPx
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f.toPx
        color = Color.RED
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 1.5f.toPx
        style = Paint.Style.STROKE
        color = Color.LTGRAY
    }

    fun setCategory(
        category: ExpenseCategory,
    ) {
        check(category.dates.size == category.amounts.size) {
            "Dates and expense amounts size should be the same"
        }

        val size = category.dates.size
        val points = ArrayList<PointF>(size)

        for (i in category.dates.indices) {
            points += PointF(
                category.dates[i].toFloat(),
                category.amounts[i].toFloat()
            )
        }

        chartData = ChartData(
            title = category.title,
            color = category.color,
            points = points,
            xMax = category.dates.maxOrNull()!!,
            yMax = category.amounts.maxOrNull()!!
        )

        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        bounds.top = paddingTop.toFloat()
        bounds.left = paddingLeft.toFloat() + offsetX
        bounds.right = (right - left).toFloat() - paddingRight
        bounds.bottom = (bottom - top).toFloat() - paddingBottom - offsetY
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(bounds, gridPaint)

        val currentState = chartData

        if (currentState != null) {
            drawPoints(canvas, currentState)
        } else {
            drawEmpty(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(widthSize, heightSize)
    }

    private fun drawPoints(canvas: Canvas, chartData: ChartData) {
        val w = bounds.width()
        val h = bounds.height()

        val path = Path()
        val points = chartData.points

        val size = max(points.size, 2)

        val xStep = w / size
        val yStep = h / size

        val yOffset = bounds.top + 10f.toPx

        val initialX = bounds.left
        val initialY = yOffset + h - ((points[0].y / chartData.yMax) * h)

        path.moveTo(initialX, initialY)

        if (points.size > 1) {
            drawAxis(initialX, initialY, points.first(), canvas)

            for (index in 1 until points.size - 1) {
                val current = points[index]

                val x = bounds.left + xStep * index
                val y = yOffset + h - ((current.y / chartData.yMax) * h)

                path.lineTo(x, y)
                drawAxis(x, y, current, canvas)
            }

            val lastX = bounds.right
            val lastY = yOffset + h - ((points.last().y / chartData.yMax) * h)

            drawAxis(
                realX = lastX - 15f.toPx,
                realY = lastY,
                point = points.last(),
                canvas = canvas
            )

            path.lineTo(lastX, lastY)
        } else {
            drawAxis(
                realX = bounds.centerX(),
                realY = initialY,
                point = points.first(),
                canvas = canvas
            )

            path.lineTo(bounds.right, initialY)
        }

        linePaint.color = chartData.color
        canvas.drawPath(path, linePaint)
    }

    private fun drawEmpty(canvas: Canvas) {
        val size = textPaint.textSize
        textPaint.textSize = 20f.toPx
        canvas.drawText(
            NO_DATA_TEXT,
            bounds.centerX(),
            bounds.centerY(),
            textPaint
        )
        textPaint.textSize = size
    }

    private fun drawAxis(
        realX: Float,
        realY: Float,
        point: PointF,
        canvas: Canvas
    ) {
        drawDate(point.x.toLong(), realX, canvas)
        drawAmount(point.y, realY, canvas)
    }

    private fun drawAmount(
        value: Float,
        y: Float,
        canvas: Canvas,
    ) {
        val x = bounds.left
        val text = "%.1f".format(value)

        textPaint.getTextBounds(text, 0, text.length, tempRect)

        canvas.drawText(
            text,
            x - tempRect.width() / 1.8f,
            y + (tempRect.height() / 2f),
            textPaint
        )
    }

    private fun drawDate(
        timestamp: Long,
        x: Float,
        canvas: Canvas,
    ) {
        val y = bounds.bottom + offsetY / 1.5f
        val text = timestamp.asDate()

        canvas.drawText(
            text,
            x,
            y,
            textPaint
        )
    }

    override fun onSaveInstanceState(): Parcelable {
        return State(super.onSaveInstanceState(), chartData)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is State) {
            super.onRestoreInstanceState(state.superState)
            chartData = state.data
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    @Parcelize
    private class ChartData(
        val title: String,
        val color: Int,
        val points: List<PointF>,
        val xMax: Long,
        val yMax: Int,
    ) : Parcelable

    @Parcelize
    private class State(
        private val baseState: Parcelable?,
        val data: ChartData?,
    ) : BaseSavedState(baseState)

    private companion object {
        const val NO_DATA_TEXT = "No data"
    }
}