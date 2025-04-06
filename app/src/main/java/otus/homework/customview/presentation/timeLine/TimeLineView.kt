package otus.homework.customview.presentation.timeLine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.presentation.timeLine.model.TimeLineData
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.mockTimeLineData

/**
 * Timeline spend diagram
 */
class TimeLineView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {

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
        println("XXX viewWidth = $viewWidth, viewHeight = $viewHeight, wSize = $wSize, hSize = $hSize")
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
            val sectorWidth = width / it.totalDays
            val verticalLinesCount = 5
            val sectorHeight = height / verticalLinesCount
            drawVerticalLines(it.spends.size, sectorWidth)
            drawHorizontalLines(verticalLinesCount, sectorHeight)
        }
    }

    private fun Canvas.drawVerticalLines(
        count: Int,
        sectorWidth: Int
    ) {
        var startX = 0f
        val startY = 0f
        var stopX = 0f
        val stopY = height.toFloat()
        repeat(count) {
            drawLine(startX, startY, stopX, stopY, chartPaint)
            startX += sectorWidth
            stopX += sectorWidth
        }
    }

    private fun Canvas.drawHorizontalLines(
        count: Int,
        sectorHeight: Int
    ) {
        val startX = 0f
        var startY = 0f
        val stopX = width.toFloat()
        var stopY = 0f
        repeat(count) {
            drawLine(startX, startY, stopX, stopY, chartPaint)
            startY += sectorHeight
            stopY += sectorHeight
        }
    }
}


