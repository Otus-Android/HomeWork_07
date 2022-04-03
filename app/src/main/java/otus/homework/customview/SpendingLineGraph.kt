package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.use
import otus.homework.customview.SpendingLineGraphHelper.SPENDING_INTERVAL
import otus.homework.customview.SpendingLineGraphHelper.calculateXMarksCount
import otus.homework.customview.SpendingLineGraphHelper.calculateYMarksCount
import otus.homework.customview.SpendingLineGraphHelper.getAllAmountsTexts
import otus.homework.customview.SpendingLineGraphHelper.getAllDatesBetweenSpendingInterval
import otus.homework.customview.SpendingLineGraphHelper.getCategoryColorToSpendingPerDate
import otus.homework.customview.utils.toDateString

class SpendingLineGraph(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private val categoriesSpending = mutableListOf<CategorySpending>()

    private var xMarksCount = 0
    private var yMarksCount = 0
    private val datesToMarkersXCoordinates = mutableMapOf<Long, Float>()
    private val markersWithColors = mutableListOf<Pair<Int, List<PointF>>>()
    private val amountsTexts = mutableListOf<String>()
    private val datesTexts = mutableListOf<String>()

    private var xMarkInterval = 0
    private var yMarkInterval = 0
    private var axisPaintWidth = 0f

    private var realXAxisLength = 0
    private var realYAxisLength = 0

    @ColorInt
    private var axisPaintColor = 0

    @ColorInt
    private var xGridPaintColor = 0

    @ColorInt
    private var yGridPaintColor = 0

    @ColorInt
    private var textColor = 0
    private var amountsMarksTextSize = 0f
    private var datesMarksTextSize = 0f
    private val amountTextBound = Rect()
    private val dateTextBound = Rect()
    private var dateTextOffset = 0

    private val gradient = LinearGradient(
        paddingStart.toFloat() + dateTextBound.width() / 2,
        paddingTop.toFloat(),
        paddingStart.toFloat() + dateTextBound.width() / 2,
        (paddingTop + realYAxisLength).toFloat(),
        context.resources.getColor(R.color.red_900, context.theme),
        context.resources.getColor(R.color.red_100, context.theme),
        Shader.TileMode.CLAMP
    )

    private val gradientPath = Path()

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val verticalGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(
            floatArrayOf(30F, 10F),
            0F
        )
    }

    private val horizontalGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(
            floatArrayOf(30F, 5F, 5F, 5F),
            0F
        )
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        alpha = 100
        shader = gradient
    }

    private val amountsTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val datesTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        context.obtainStyledAttributes(attributeSet, R.styleable.SpendingLineGraph).use { typedArray ->
            axisPaintColor = typedArray.getColor(
                R.styleable.SpendingLineGraph_axis_color,
                context.resources.getColor(R.color.black, context.theme)
            )
            xGridPaintColor = typedArray.getColor(
                R.styleable.SpendingLineGraph_axis_color,
                context.resources.getColor(R.color.blue_gray_400, context.theme)
            )
            yGridPaintColor = typedArray.getColor(
                R.styleable.SpendingLineGraph_axis_color,
                context.resources.getColor(R.color.blue_gray_400, context.theme)
            )
            textColor = typedArray.getColor(
                R.styleable.SpendingLineGraph_text_color,
                context.resources.getColor(R.color.blue_gray_400, context.theme)
            )
        }

        xMarkInterval = context.resources.getDimensionPixelSize(R.dimen.x_mark_interval)
        yMarkInterval = context.resources.getDimensionPixelSize(R.dimen.y_mark_interval)
        axisPaintWidth = context.resources.getDimension(R.dimen.axis_paint_width)
        amountsMarksTextSize = context.resources.getDimension(R.dimen.line_graph_amounts_text_size)
        datesMarksTextSize = context.resources.getDimension(R.dimen.line_graph_dates_text_size)
        dateTextOffset = context.resources.getDimensionPixelSize(R.dimen.date_text_offset)

        configurePaints()
    }

    private fun configurePaints() {
        axisPaint.apply {
            strokeWidth = axisPaintWidth
            color = axisPaintColor
        }

        verticalGridPaint.apply {
            color = xGridPaintColor
        }

        horizontalGridPaint.apply {
            color = yGridPaintColor
        }

        linePaint.apply {
            strokeWidth = axisPaintWidth * 2
        }

        gradientPaint.apply {
            strokeWidth = axisPaintWidth * 2
        }

        amountsTextPaint.apply {
            textSize = amountsMarksTextSize
            color = textColor
        }
        datesTextPaint.apply {
            textSize = datesMarksTextSize
            color = textColor
        }
    }

    fun setData(spending: List<CategorySpending>) {
        categoriesSpending.addAll(spending)

        xMarksCount = calculateXMarksCount(spending)
        yMarksCount = calculateYMarksCount(spending)
        fillAmountsTexts(spending)
        fillDatesTexts(spending)
        requestLayout()
        invalidate()
    }

    private fun fillAmountsTexts(spending: List<CategorySpending>) {
        val allAmounts: List<String> = getAllAmountsTexts(spending)
            .map {
                resources.getString(R.string.amount_text, it)
                    .also { amountString -> amountsTexts.add(amountString) }
            }
        val longestString: String = allAmounts.maxByOrNull { it.length } ?: allAmounts.last()
        amountsTextPaint.getTextBounds(longestString, 0, longestString.length, amountTextBound)
    }

    private fun fillDatesTexts(spending: List<CategorySpending>) {
        val allDates: List<String> = getAllDatesBetweenSpendingInterval(spending)
            .map {
                it.toDateString()
                    .also { dateString -> datesTexts.add(dateString) }
            }
        val oneDateString = allDates.first()
        datesTextPaint.getTextBounds(oneDateString, 0, oneDateString.length, dateTextBound)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(
            TAG, "onMeasure() called with: " +
                "widthMeasureSpec = ${MeasureSpec.toString(widthMeasureSpec)}, " +
                "heightMeasureSpec = ${MeasureSpec.toString(heightMeasureSpec)}"
        )

        val resultWidth: Int
        val resultHeight: Int

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        resultWidth = if (widthMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            xMarkInterval * (xMarksCount - 1) + paddingStart + paddingEnd + amountTextBound.width() + dateTextBound.width()
        }


        resultHeight = if (heightMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            yMarkInterval * (yMarksCount - 1) + paddingTop + paddingEnd + dateTextOffset + dateTextBound.height()
        }

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        realXAxisLength = measuredWidth - paddingStart - paddingEnd - amountTextBound.width() - dateTextBound.width()
        realYAxisLength = measuredHeight - paddingTop - paddingBottom - dateTextOffset - dateTextBound.height()

        xMarkInterval = realXAxisLength / (xMarksCount - 1)
        yMarkInterval = realYAxisLength / (yMarksCount - 1)

        calculateMarkersXCoordinates()
        fillMarkersWithColors()
    }

    private fun calculateMarkersXCoordinates() {
        val allDates = getAllDatesBetweenSpendingInterval(categoriesSpending)
        allDates.forEachIndexed { index, date ->
            datesToMarkersXCoordinates[date] =
                (paddingStart + xMarkInterval * index + dateTextBound.width() / 2).toFloat()
        }
    }

    private fun fillMarkersWithColors() {
        val categoriesSpending = getCategoryColorToSpendingPerDate(categoriesSpending)
        categoriesSpending.forEach {
            calculateMarkers(it.key, it.value)
        }
    }

    private fun calculateMarkers(colorRes: Int, categorySpending: Map<Long, Float>) {
        val categoryMarkers = mutableListOf<PointF>()
        datesToMarkersXCoordinates.forEach {
            // найти трату на дату [it.key]
            val daySpending = categorySpending[it.key] ?: 0f

            val yPos = paddingTop + realYAxisLength - ((daySpending / SPENDING_INTERVAL) * yMarkInterval)
            categoryMarkers.add(PointF(it.value, yPos))
        }
        markersWithColors.add(Pair(colorRes, categoryMarkers))
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d(TAG, "measuredWidth: $measuredWidth, measuredHeight: $measuredHeight")

        canvas?.apply {
            drawAxis()
            drawGrid()
            drawTexts()
            drawAllGradients()
            drawAllLines()
        }
    }

    private fun Canvas.drawAxis() {
        drawLine(
            paddingStart.toFloat() + dateTextBound.width() / 2,
            paddingTop + realYAxisLength.toFloat(),
            paddingStart + realXAxisLength.toFloat() + dateTextBound.width() / 2,
            paddingTop + realYAxisLength.toFloat(),
            axisPaint
        )
        drawLine(
            paddingStart + realXAxisLength.toFloat() + dateTextBound.width() / 2,
            paddingTop.toFloat(),
            paddingStart + realXAxisLength.toFloat() + dateTextBound.width() / 2,
            paddingTop + realYAxisLength.toFloat(),
            axisPaint
        )
    }

    private fun Canvas.drawGrid() {
        // draw vertical grid
        var startX = paddingStart + xMarkInterval + dateTextBound.width() / 2
        repeat(xMarksCount - 2) {
            drawLine(
                startX.toFloat(),
                paddingTop.toFloat(),
                startX.toFloat(),
                paddingTop + realYAxisLength.toFloat(),
                verticalGridPaint
            )
            startX += xMarkInterval
        }

        // draw horizontal grid
        var startY = paddingStart + yMarkInterval
        repeat(yMarksCount - 2) {
            drawLine(
                paddingStart.toFloat() + dateTextBound.width() / 2,
                startY.toFloat(),
                paddingStart + realXAxisLength.toFloat() + dateTextBound.width() / 2,
                startY.toFloat(),
                horizontalGridPaint
            )
            startY += yMarkInterval
        }
    }

    private fun Canvas.drawTexts() {
        // текста по оси X
        datesTexts.forEachIndexed { index, date ->
            drawText(
                date,
                (paddingStart + xMarkInterval * index).toFloat(),
                (paddingTop + realYAxisLength + dateTextBound.height() + dateTextOffset).toFloat(),
                datesTextPaint.apply { textSize = datesMarksTextSize }
            )
        }

        // текста по оси Y
        amountsTexts.forEachIndexed { index, amount ->
            drawText(
                amount,
                paddingStart + realXAxisLength.toFloat() + dateTextBound.width() / 2,
                paddingTop + (realYAxisLength - yMarkInterval * index).toFloat(),
                amountsTextPaint
            )
        }
    }

    private fun Canvas.drawAllGradients() {
        markersWithColors.forEach {
            drawGradient(it.second)
        }
    }

    private fun Canvas.drawAllLines() {
        markersWithColors.forEach {
            drawMarkersAndLine(it.first, it.second)
        }
    }

    private fun Canvas.drawMarkersAndLine(@ColorRes lineColor: Int, markers: List<PointF>) {
        linePaint.apply { color = context.resources.getColor(lineColor, context.theme) }
        var previousMarker: PointF? = null
        for (marker in markers) {
            if (previousMarker != null) {
                // draw the line
                drawLine(previousMarker.x, previousMarker.y, marker.x, marker.y, linePaint)
            }

            previousMarker = marker
            // draw the marker
            drawCircle(
                marker.x,
                marker.y,
                MARKER_RADIUS,
                linePaint
            )
        }
    }

    private fun Canvas.drawGradient(markers: List<PointF>) {
        gradientPath.reset()
        gradientPath.moveTo(
            paddingStart.toFloat() + dateTextBound.width() / 2,
            (paddingTop + realYAxisLength).toFloat()
        )

        for (marker in markers) {
            gradientPath.lineTo(marker.x, marker.y)
        }

        // close the path
        gradientPath.lineTo(markers.last().x, (paddingTop + realYAxisLength).toFloat())
        gradientPath.lineTo(
            paddingStart.toFloat() + dateTextBound.width() / 2,
            (paddingTop + realYAxisLength).toFloat()
        )

        drawPath(gradientPath, gradientPaint)
    }

    private companion object {
        const val TAG = "SpendingLineGraph"
        const val MARKER_RADIUS = 3f
    }
}