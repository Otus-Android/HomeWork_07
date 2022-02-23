package alektas.views.line_chart

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// TODO: remove hardcode, add styleable attributes and customization
class LineChart(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultViewWidth = 500
    private val defaultViewHeight = 300
    private val valueGridStepX = 1f
    private val valueGridStepY = 1000f
    private var pxToValueRatioX = 1f
    private var pxToValueRatioY = 1f

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val gridLabelPaint = Paint().apply {
        isAntiAlias = true
        color = Color.LTGRAY
        textSize = 36f
    }
    private val axisPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val axisLabelPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 50f
    }
    private val dataSetLinePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val selectedPointOuterPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val selectedPointInnerPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var verticalLines: List<LineF> = emptyList()
    private var horizontalLines: List<LineF> = emptyList()

    var gridLabelPatternX = "%.0f"
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }
    var gridLabelPatternY = "%.0f"
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var xAxisLabel = "X"
        set(value) {
            field = value
            xAxisLabelOffsetX = -axisLabelPaint.measureText(value)
            invalidate()
        }
    private var xAxisLabelOffsetX = -axisLabelPaint.measureText(xAxisLabel)
    private var xAxisLabelOffsetY = -20f

    var yAxisLabel = "Y"
        set(value) {
            field = value
            yAxisLabelOffsetY = axisLabelPaint.textSize
            invalidate()
        }
    private var yAxisLabelOffsetX = 20f
    private var yAxisLabelOffsetY = axisLabelPaint.textSize

    private var gridVerticalLabelsOffsetY = gridLabelPaint.textSize + 20f
    private var gridVerticalLabelsOffsetX = 0f
    private var gridHorizontalLabelsOffsetY = -20f
    private var gridHorizontalLabelsOffsetX = 20f

    private var dataSetPointPositions: List<PointF> = emptyList()
    var dataSet: LineChartDataSet = LineChartDataSet(0, "", emptyList(), Color.WHITE)
        set(value) {
            field = value
            dataSetLinePaint.color = value.color
            selectedPointOuterPaint.color = value.color
            calculateDimensions(measuredWidth, measuredHeight)
        }
    var selectedPointId: Int? = 6 // TODO: just for example of view data persisting
    private var selectedPointRadius: Float = 16f

    init {
        setBackgroundColor(Color.DKGRAY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = max(defaultViewWidth, suggestedMinimumWidth + paddingLeft + paddingRight)
        val desiredHeight = max(defaultViewHeight, suggestedMinimumHeight + paddingTop + paddingBottom)
        val size = min(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateDimensions(w, h)
    }

    private fun calculateDimensions(width: Int, height: Int) {
        if (width == 0 || height == 0) return

        val leftX = paddingLeft.toFloat()
        val rightX = width - paddingRight.toFloat()
        val bottomY = height - paddingBottom.toFloat()
        val topY = paddingTop.toFloat()

        pxToValueRatioX = calculatePixelToValueRatio(leftX, rightX) { point -> point.valueX } ?: return
        calculateVerticalLines(pxToValueRatioX, leftX, rightX, topY, bottomY)

        pxToValueRatioY = calculatePixelToValueRatio(bottomY, topY) { point -> point.valueY } ?: return
        calculateHorizontalLines(pxToValueRatioY, leftX, rightX, topY, bottomY)

        calculateDataSetPoints(pxToValueRatioX, pxToValueRatioY, bottomY)

        invalidate()
    }

    private fun calculateVerticalLines(
        pxToValueRatio: Float,
        leftX: Float,
        rightX: Float,
        topY: Float,
        bottomY: Float
    ) {
        verticalLines = buildList {
            val gridStepX = valueGridStepX * pxToValueRatio
            var x = leftX
            while (x <= rightX) {
                add(
                    LineF(
                        startPoint = PointF(x, bottomY),
                        endPoint = PointF(x, topY),
                        label = gridLabelPatternX.format(x / pxToValueRatio)
                    )
                )
                x += gridStepX
            }
        }
    }

    private fun calculateHorizontalLines(
        pxToValueRatio: Float,
        leftX: Float,
        rightX: Float,
        topY: Float,
        bottomY: Float,
    ) {
        horizontalLines = buildList {
            val minValueY = dataSet.points.minOfOrNull { it.valueY } ?: 0f
            val gridStepY = valueGridStepY * pxToValueRatio
            var y = bottomY
            while (y >= topY) {
                add(
                    LineF(
                        startPoint = PointF(leftX, y),
                        endPoint = PointF(rightX, y),
                        label = gridLabelPatternY.format((bottomY - y) / pxToValueRatio + minValueY)
                    )
                )
                y -= gridStepY
            }
        }
    }

    private fun calculatePixelToValueRatio(
        minDimension: Float,
        maxDimension: Float,
        valueSelector: (LineChartPoint) -> Float
    ): Float? = with(dataSet.points) {
        val minValue = minOfOrNull(valueSelector) ?: return null
        val maxValue = maxOfOrNull(valueSelector) ?: return null
        return abs(maxDimension - minDimension) / abs(maxValue - minValue)
    }

    private fun calculateDataSetPoints(pxToValueRatioX: Float, pxToValueRatioY: Float, bottomY: Float) {
        dataSetPointPositions = dataSet.points.map { point ->
            PointF(point.valueX * pxToValueRatioX, bottomY - point.valueY * pxToValueRatioY)
        }
    }

    override fun onDraw(canvas: Canvas) = with(canvas) {
        drawGrid()
        drawDataSetLine()
        drawSelectedPoint()
        drawAxisX()
        drawAxisY()
    }

    private fun Canvas.drawDataSetLine() {
        val line = Path().apply {
            dataSetPointPositions.forEachIndexed { i, point ->
                if (i == 0) {
                    moveTo(point.x, point.y)
                } else {
                    val previous = dataSetPointPositions[i - 1]
                    val anchorX = previous.x + (point.x - previous.x) / 2f
                    cubicTo(
                        anchorX, previous.y,
                        anchorX, point.y,
                        point.x, point.y
                    )
                }
            }
        }
        drawPath(line, dataSetLinePaint)
    }

    private fun Canvas.drawSelectedPoint() {
        selectedPointId?.let { id ->
            dataSet.points.find { it.id == id }?.let { point ->
                val x = point.valueX * pxToValueRatioX
                val y = measuredHeight - paddingBottom.toFloat() - point.valueY * pxToValueRatioY
                drawCircle(x, y, selectedPointRadius, selectedPointInnerPaint)
                drawCircle(x, y, selectedPointRadius, selectedPointOuterPaint)
            }
        }
    }

    private fun Canvas.drawGrid() {
        verticalLines.forEach { (startPoint, endPoint, label) ->
            drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, gridPaint)
            drawText(
                label,
                startPoint.x + gridVerticalLabelsOffsetX,
                startPoint.y + gridVerticalLabelsOffsetY,
                gridLabelPaint
            )
        }
        horizontalLines.forEach { (startPoint, endPoint, label) ->
            drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, gridPaint)
            drawText(
                label,
                startPoint.x + gridHorizontalLabelsOffsetX,
                startPoint.y + gridHorizontalLabelsOffsetY,
                gridLabelPaint
            )
        }
    }

    private fun Canvas.drawAxisX() {
        val axisY = measuredHeight - paddingBottom.toFloat()
        val axisEndX = measuredWidth - paddingRight.toFloat()
        drawLine(paddingLeft.toFloat(), axisY, axisEndX, axisY, axisPaint)
        drawText(xAxisLabel, axisEndX + xAxisLabelOffsetX, axisY + xAxisLabelOffsetY, axisLabelPaint)
    }

    private fun Canvas.drawAxisY() {
        val axisStartY = paddingTop.toFloat()
        val axisEndY = measuredHeight - paddingBottom.toFloat()
        val axisX = paddingLeft.toFloat()
        drawLine(axisX, axisStartY, axisX, axisEndY, axisPaint)
        drawText(yAxisLabel, axisX + yAxisLabelOffsetX, axisStartY + yAxisLabelOffsetY, axisLabelPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            selectedPointId?.let {
                savedSelectedPointId = it
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            selectedPointId = state.savedSelectedPointId
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {

        var savedSelectedPointId: Int? = null

        constructor(parcelable: Parcelable?) : super(parcelable)

        constructor(source: Parcel?) : super(source) {
            source?.run {
                savedSelectedPointId = readInt()
            }
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.apply {
                savedSelectedPointId?.let { writeInt(it) }
            }
        }

        companion object {

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel?): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

    }

    private data class LineF(val startPoint: PointF, val endPoint: PointF, val label: String)

}
