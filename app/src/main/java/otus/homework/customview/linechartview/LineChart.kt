package otus.homework.customview.linechartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.utils.getLocalDateFromLong
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@SuppressLint("NewApi")
class LineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : View(context, attrs, defStyleAttrs) {

    //<editor-fold desc="Dimensions">
    private var zero: PointF = PointF()

    private val numberOfRows = 10

    private var numberOfDays = 10

    private var colWidth = 0f

    private val maxXSteps = 10

    private val xOffset
        get() = horizontalMargin + yLegendWidth

    private val yOffset
        get() = verticalMargin + legendYHeight

    private var yStep = 1000

    private var maxAmount = 0.0

    private var marginBetweenYAxisAndLegend = 12f

    private var marginBetweenXAxisAndLegend = 12f

    private val verticalMargin: Float = resources.getDimension(R.dimen.line_chart_vertical_margin)

    private val dashCircleRadius: Float =
        resources.getDimension(R.dimen.line_chart_dash_circle_radius)

    private val horizontalMargin: Float =
        resources.getDimension(R.dimen.line_chart_horizontal_margin)

    private var defaultHeight: Int =
        resources.getDimensionPixelSize(R.dimen.line_chart_default_height)

    private val lineStrokeWidth = resources.getDimension(R.dimen.line_chart_line_width)

    private var defaultWidth: Int =
        resources.getDimensionPixelSize(R.dimen.line_chart_default_width)

    private var xLegendTextHeight = resources.getDimension(R.dimen.line_chart_x_legend_text_size)

    private var yLegendTextHeight = resources.getDimension(R.dimen.line_chart_y_legend_text_size)

    private var categoryNameTextSize = resources.getDimension(R.dimen.line_chart_category_text_size)

    private var categoryDateTextSize = resources.getDimension(R.dimen.line_chart_date_text_size)

    private var yLegendWidth = resources.getDimension(R.dimen.line_chart_y_legend_width)

    private val legendYHeight: Float
        get() = xLegendTextHeight * 2

    private var rowHeight = (defaultHeight - verticalMargin - yOffset) / numberOfRows

    private val dashLength = resources.getDimension(R.dimen.line_chart_dash_length)
    //</editor-fold>

    //<editor-fold desc="Graphics">
    private val paintAxis = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GRAY
        alpha = 255
        strokeWidth = resources.getDimension(R.dimen.line_chart_line_width)
    }

    private val paintYLegend = Paint().apply {
        isAntiAlias = true
        textSize = yLegendTextHeight
        color = Color.GRAY
        textAlign = Paint.Align.RIGHT
    }

    private val paintXLegend = Paint().apply {
        isAntiAlias = true
        textSize = xLegendTextHeight
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
    }

    private val paintAuxRows = Paint().apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(dashLength, dashLength), 0f)
        color = Color.LTGRAY
        strokeWidth = resources.getDimension(R.dimen.line_chart_dash_line_width)
        alpha = 80
    }

    private val categoryCirclesPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val categoryCircleStrokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = lineStrokeWidth
    }

    private val categoryNamePaint = Paint().apply {
        isAntiAlias = true
        textSize = categoryNameTextSize
        color = Color.DKGRAY
    }

    private val categoryDatePaint = Paint().apply {
        isAntiAlias = true
        textSize = categoryDateTextSize
        color = Color.GRAY
    }

    private var modelPaint = Paint()

    private val path = Path()

    //</editor-fold>

    //<editor-fold desc="Data">
    private var lineData: LineData? = null

    private val verticalDashes = mutableListOf<Dash<Int>>()

    private val horizontalDashes = mutableListOf<Dash<Int>>()

    private var startDate: LocalDate = LocalDate.now()

    private var endDate: LocalDate = LocalDate.now()
    //</editor-fold>

    fun setLineData(lineData: LineData?) {
        this.lineData = lineData
        requestLayout()
        calculateDimensions()
        invalidate()
    }

    @SuppressLint("NewApi")
    private fun calculateDimensions() {
        maxAmount = lineData?.maxAmount ?: error("Max amount can't be null")

        yStep = (maxAmount / (numberOfRows - 2)).toInt()

        val minTime = lineData?.minTime ?: error("Min time cannot be null")
        val maxTime = lineData?.maxTime ?: error("Max time cannot be null")
        startDate = getLocalDateFromLong(minTime).minusDays(1)
        endDate = getLocalDateFromLong(maxTime)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                defaultWidth
            }
            MeasureSpec.AT_MOST -> {
                defaultWidth.coerceAtMost(widthSpecSize)
            }
            MeasureSpec.EXACTLY -> {
                widthSpecSize
            }
            else -> {
                defaultWidth.coerceAtMost(widthSpecSize)
            }
        }

        val height: Int = when (heightMode) {
            MeasureSpec.UNSPECIFIED -> {
                defaultHeight
            }
            MeasureSpec.AT_MOST -> {
                defaultHeight.coerceAtMost(heightSpecSize)
            }
            MeasureSpec.EXACTLY -> {
                heightSpecSize
            }
            else -> {
                defaultHeight.coerceAtMost(heightSpecSize)
            }
        }
        rowHeight =
            ((height - 2 * verticalMargin - yOffset) / numberOfRows)

        colWidth = (width - 2 * horizontalMargin - xOffset) / numberOfDays

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateLineChartPositions()
    }

    private fun updateLineChartPositions() {
        updateXDashes()
        updateYDashes()
        calculateLineDataPositions()
        updateZeroPosition()
    }

    private fun updateZeroPosition() {
        zero.apply {
            x = xOffset
            y = height - yOffset
        }
    }

    private fun calculateLineDataPositions() {
        lineData?.lineModels?.forEach { entry ->
            calculatePositionsForLineModel(entry.value)
        }
    }

    private fun calculatePositionsForLineModel(lineModel: LineModel) {
        lineModel.positions.forEach { dayPos ->
            calculatePositionForDay(dayPos)
        }
    }

    private fun calculatePositionForDay(dayPosition: DayPosition) {
        val modelDate = dayPosition.day
        val xPos = (startDate.until(modelDate, ChronoUnit.DAYS)) * colWidth + xOffset
        val yPos =
            (height - (dayPosition.amount / yStep) * rowHeight - verticalMargin - yOffset).toFloat()
        dayPosition.position.x = xPos
        dayPosition.position.y = yPos
    }

    private fun updateYDashes() {
        var startAmount = yStep
        for (i in 0 until numberOfRows) {
            verticalDashes.add(
                Dash(
                    spec = startAmount
                )
            )
            startAmount += yStep
        }
    }

    private fun updateXDashes() {
        var startDay = startDate
        for (i in 0 until maxXSteps) {
            horizontalDashes.add(
                Dash(
                    spec = startDay.dayOfWeek.value
                )
            )
            startDay = startDay.plusDays(1)
        }
    }

    override fun onDraw(canvas: Canvas) {
        with(canvas) {
            drawAxis()
            drawYAxisDashesAndAuxLines()
            drawXAxisDashes()
            drawChartLines()
        }
    }

    private fun Canvas.drawChartLines() {
        lineData?.lineModels?.forEach { entry ->
            drawLineModel(this, entry.value)
        }
    }

    private fun drawLineModel(canvas: Canvas, lineModel: LineModel) {
        var index = 0
        path.reset()
        path.moveTo(zero.x, zero.y)
        modelPaint = lineModel.paint.apply {
            strokeWidth = lineStrokeWidth
        }
        for (model in lineModel.positions) {
            // ensure to stay within bounds
            if (index == maxXSteps) {
                break
            }
            path.lineTo(model.position.x, model.position.y)
            canvas.drawPath(path, modelPaint)
            ++index
        }
    }

    private fun Canvas.drawAxis() {
        drawLine(zero.x, verticalMargin, zero.x, zero.y, paintAxis)
        drawLine(zero.x, zero.y, width - horizontalMargin, zero.y, paintAxis)
        drawCircle(
            zero.x,
            zero.y,
            dashCircleRadius,
            paintYLegend
        )
    }

    private fun Canvas.drawYAxisDashesAndAuxLines() {
        repeat(numberOfRows) { row ->
            val dash = verticalDashes[row]
            val maxWidth = yLegendWidth - marginBetweenYAxisAndLegend
            val charsCount =
                paintYLegend.breakText(dash.spec.toString(), true, maxWidth, null)
            val yPos = zero.y - (row + 1) * rowHeight
            val textY = paintYLegend.getTextBaselineByCenter(yPos)
            drawText(
                dash.spec.toString().substring(0, charsCount),
                zero.x - marginBetweenYAxisAndLegend,
                textY,
                paintYLegend
            )
            drawCircle(
                zero.x,
                yPos,
                dashCircleRadius,
                paintYLegend
            )
            val endX = width - horizontalMargin
            drawLine(zero.x, yPos, endX, yPos, paintAuxRows)
        }
    }

    private fun Canvas.drawXAxisDashes() {
        repeat(numberOfDays) { col ->
            val dash = horizontalDashes[col]
            val xPos = zero.x + (col + 1) * colWidth
            val yPos = zero.y + xLegendTextHeight + marginBetweenXAxisAndLegend
            drawText(
                dash.spec.toString(),
                xPos,
                yPos,
                paintXLegend
            )
            drawCircle(
                xPos,
                zero.y,
                dashCircleRadius,
                paintYLegend
            )
        }
    }

    private fun Paint.getTextBaselineByCenter(center: Float) = center - (descent() + ascent()) / 2

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).also {
            it.lineData = lineData
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when(state) {
            is SavedState -> {
                super.onRestoreInstanceState(state)
                updateChart(state)
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    private fun updateChart(savedState: SavedState) {
        lineData = savedState.lineData
        requestLayout()
        invalidate()
    }

    private class SavedState : BaseSavedState {

        var lineData: LineData? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel?) : super(source) {
            source?.apply {
                lineData = source.readParcelable(LineData::class.java.classLoader)
            }
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeParcelable(lineData, 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel?): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

}

private data class Dash<T>(
    val spec: T,
)















