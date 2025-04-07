package otus.homework.customview.presentation.timeLine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.presentation.timeLine.model.TimeLineData
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.mockTimeLineData
import java.text.NumberFormat

/**
 * Timeline spend diagram
 */
class TimeLineView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {

    private var verticalLinesCount = 5

    private var timeLineData: TimeLineData? = null
    private var desiredSize = 1000.dp
    private var center = PointF()

    private val selectedPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val otherCategoriesPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val chartPaint = Paint().apply {
        color = Color.GRAY
        alpha = 200
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
    }

    private val mainTextPaint = TextPaint().apply {
        color = Color.GRAY
        isAntiAlias = true
        typeface = Typeface.SANS_SERIF
    }

    private val path = Path()

    init {
        if (isInEditMode) {
            timeLineData = mockTimeLineData
        }
    }

    fun setData(timeLineData: TimeLineData) {
        this.timeLineData = timeLineData

        requestLayout()
        invalidate()
    }

    fun setVerticalLinesCount(count: Int) {
        verticalLinesCount = count
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)

        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidthWithPadding = desiredSize + paddingLeft + paddingRight
        val desiredHeightWithPadding = desiredSize + paddingTop + paddingBottom

        val viewWidth: Int = when (wMode) {
            MeasureSpec.AT_MOST -> desiredWidthWithPadding.coerceAtMost(wSize)
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.UNSPECIFIED -> desiredWidthWithPadding
            else -> desiredWidthWithPadding
        }

        val viewHeight: Int = when (hMode) {
            MeasureSpec.AT_MOST -> desiredHeightWithPadding.coerceAtMost(hSize)
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.UNSPECIFIED -> desiredHeightWithPadding
            else -> desiredHeightWithPadding
        }
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(width / 2f, height / 2f)

        setElementsSize()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawChart()
    }

    private fun setElementsSize() {
        selectedPaint.strokeWidth = height / 80f
        otherCategoriesPaint.strokeWidth = height / 80f
        chartPaint.strokeWidth = height / 500f
        mainTextPaint.textSize = height / 20f
    }

    private fun Canvas.drawChart() {
        timeLineData?.let {
            val sectorWidth = (width - paddingStart - paddingEnd) / it.totalDays.size
            drawVerticalLines(it.totalDays.size, sectorWidth, it.totalDays)

            val sectorHeight = (height - paddingTop - paddingBottom) / verticalLinesCount
            val sectorAmount = it.maxAmount / verticalLinesCount
            drawHorizontalLines(verticalLinesCount, sectorHeight, sectorAmount)

            it.spends.forEach { entry ->
                val points = entry.value.map { spendModel ->
                    PointF(
                        /* x = */ spendModel.part.first * sectorWidth + paddingStart,
                        /* y = */ (1f - spendModel.part.second) * verticalLinesCount * sectorHeight - paddingBottom
                    )
                }
                println("XXX Points = $points")
                val paint = if (entry.key == it.selected) selectedPaint else otherCategoriesPaint
                drawSmoothPath(points, paint, path)
            }
        }
    }

    private fun Canvas.drawVerticalLines(
        count: Int,
        sectorWidth: Int,
        totalDays: Set<String>
    ) {
        var startX = 0f + paddingLeft
        val startY = 0f + paddingTop
        var stopX = 0f + paddingStart
        val stopY = height.toFloat() - paddingBottom
        repeat(count) {
            drawLine(startX, startY, stopX, stopY, chartPaint)
            drawDate(it, totalDays, stopX, stopY)
            startX += sectorWidth
            stopX += sectorWidth
        }
    }

    private fun Canvas.drawDate(
        count: Int,
        totalDays: Set<String>,
        x: Float,
        y: Float
    ) {
        if (count != 0 && count % 2 == 0) {
            val dateString = totalDays.elementAt(count)
            val width = mainTextPaint.measureText(dateString)
            drawText(dateString, x - width / 2, y, mainTextPaint)
        }
    }

    private fun Canvas.drawHorizontalLines(
        count: Int,
        sectorHeight: Int,
        sectorAmount: Int,
    ) {
        val startX = 0f + paddingLeft
        var startY = 0f + paddingTop
        val stopX = width.toFloat() - paddingRight
        var stopY = 0f + paddingTop
        repeat(count) {
            drawLine(startX, startY, stopX, stopY, chartPaint)
            drawAmount(sectorAmount, count, it, startX, stopY)
            startY += sectorHeight
            stopY += sectorHeight
        }
        drawLine(startX, startY, stopX, stopY, chartPaint)
    }

    private fun Canvas.drawAmount(
        sectorAmount: Int,
        count: Int,
        it: Int,
        x: Float,
        y: Float
    ) {
        val drawingText = buildString {
            append(NumberFormat.getInstance().format(sectorAmount * (count - it)))
            append(" $")
        }
        drawText(drawingText, x, y + mainTextPaint.textSize, mainTextPaint)
    }

    private fun Canvas.drawSmoothPath(points: List<PointF>, paint: Paint, path: Path) {
        if (points.size < 2) return

        path.moveTo(points[0].x, points[0].y)

        for (i in 1 until points.lastIndex) {
            val midPointX = (points[i].x + points[i + 1].x) / 2
            val midPointY = (points[i].y + points[i + 1].y) / 2
            path.quadTo(points[i].x, points[i].y, midPointX, midPointY)
        }

        val last = points.last()
        path.lineTo(last.x, last.y)

        drawPath(path, paint)
        path.reset()
    }
}