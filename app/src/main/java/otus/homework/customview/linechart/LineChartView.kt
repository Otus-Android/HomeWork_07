package otus.homework.customview.linechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.google.android.material.color.MaterialColors
import me.moallemi.tools.daterange.date.rangeTo
import otus.homework.customview.R
import otus.homework.customview.common.ColorUtils
import otus.homework.customview.common.atDayStart
import otus.homework.customview.common.getDayOfMonth
import otus.homework.customview.common.getMonthName
import java.util.Date
import kotlin.properties.Delegates

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var minWidth: Int by Delegates.notNull()
    private var minHeight: Int by Delegates.notNull()
    private var valueStep: Float by Delegates.notNull()
    private var axisTextSize: Float by Delegates.notNull()

    private val baseColor = MaterialColors.getColor(context, R.attr.axisColor, Color.BLACK)

    init {
        readAttrs(context, attrs, defStyleAttr)
    }

    private var firstDate: Date = Date()
    private var lastDate: Date = Date()

    private var daysCount: Int = 0
    private var lineCount: Int = 0

    private val axisPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = baseColor
    }

    private val xAxisLabelPaint = Paint().apply {
        textSize = axisTextSize
        color = baseColor
    }

    private val yAxisLabelPaint = Paint().apply {
        textSize = axisTextSize
        color = baseColor
        textAlign = Paint.Align.RIGHT
    }

    private val graphLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = CornerPathEffect(10f)
    }

    private val xLabelBounds = Rect()
    private val yLabelBounds = Rect()

    private val strokeOffset = axisPaint.strokeWidth / 2
    private var verticalsInterval = 0f
    private var horizontalInterval = 0f

    private var items = emptyList<LineChartItem>()
    private var ranges = emptyList<LineChartRange>()

    fun setLineChartItems(items: List<LineChartItem>) {
        this.items = items
        calculateLineChartRanges()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = calculateMeasureSize(widthMode, widthSize, minWidth)
        val height = calculateMeasureSize(heightMode, heightSize, minHeight)

        verticalsInterval = (width - strokeOffset * 2) / (daysCount - 1)
        horizontalInterval =
            (height - axisTextSize - strokeOffset * 2) / lineCount.toFloat()

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawAxis(canvas)
        drawLabels(canvas)
        drawRanges(canvas)
    }

    private fun calculateLineChartRanges() {
        if (items.isEmpty()) return

        val (firstDate, lastDate) = calculateFirstAndLastDate()

        this.firstDate = firstDate
        this.lastDate = lastDate

        val itemsByCategory = items.groupBy { it.category }
        val maxPrice = itemsByCategory.maxOf { category ->
            category.value.sumOf { it.amount }
        }

        daysCount = calculateDaysBetweenTwoDate(firstDate, lastDate) + 1
        lineCount = (maxPrice / valueStep + 1).toInt()

        calculateUnderGraphLabelBound()
        calculateRightSideLabelBound()

        ranges = itemsByCategory.values.map { category ->
            val pointsList = mutableListOf<PointF>()
            val categoryByDate =
                category.groupBy { getDateWithoutTimeFromTimestampInSeconds(it.time) }

            firstDate.rangeTo(lastDate).forEach { date ->
                val currentDateCategory = categoryByDate[date]
                if (currentDateCategory == null) {
                    pointsList.add(
                        PointF(calculateDaysBetweenTwoDate(firstDate, date).toFloat(), 0f)
                    )
                } else {
                    pointsList.add(
                        PointF(
                            calculateDaysBetweenTwoDate(firstDate, date).toFloat(),
                            currentDateCategory.sumOf { it.amount } / valueStep)
                    )
                }
            }

            return@map LineChartRange(pointsList, ColorUtils.getRandomColor())
        }
    }

    private fun calculateFirstAndLastDate(): Pair<Date, Date> {
        val sortedItemsByTime = items.sortedBy { it.time }

        val firstDate = getDateWithoutTimeFromTimestampInSeconds(
            sortedItemsByTime.first().time
        )

        val lastDate = getDateWithoutTimeFromTimestampInSeconds(
            sortedItemsByTime.last().time
        )

        return firstDate to lastDate
    }

    private fun calculateUnderGraphLabelBound() {
        xAxisLabelPaint.getTextBounds(
            DATE_LABEL_TEMPLATE,
            0,
            DATE_LABEL_TEMPLATE.length,
            xLabelBounds
        )
    }

    private fun calculateRightSideLabelBound() {
        val priceDelimiterAsString = valueStep.toString()

        yAxisLabelPaint.getTextBounds(
            priceDelimiterAsString,
            0,
            priceDelimiterAsString.length,
            yLabelBounds
        )
    }

    private fun calculateMeasureSize(mode: Int, size: Int, minSize: Int): Int {
        return when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)

            else -> throw IllegalStateException("Unsupported MeasureSpec")
        }
    }

    private fun drawAxis(canvas: Canvas) {
        drawHorizontalAxis(canvas)
        drawVerticalAxis(canvas)
    }

    private fun drawHorizontalAxis(canvas: Canvas) {
        (0 until daysCount).forEach { day ->
            val xVertical = day * verticalsInterval + strokeOffset
            canvas.drawLine(
                xVertical,
                0f,
                xVertical,
                height.toFloat() - axisTextSize,
                axisPaint
            )
        }
    }

    private fun drawVerticalAxis(canvas: Canvas) {
        (0..lineCount).forEach { price ->
            val yHorizontal = price * horizontalInterval
            canvas.drawLine(
                0f,
                yHorizontal,
                width.toFloat(),
                yHorizontal,
                axisPaint
            )
        }
    }

    private fun drawLabels(canvas: Canvas) {
        drawXAxisGraphLabel(canvas)
        drawYAxisGraphLabel(canvas)
    }

    private fun drawXAxisGraphLabel(canvas: Canvas) {
        (firstDate..lastDate).forEachIndexed { index, date ->
            val xOffset = when (date) {
                firstDate -> 0
                lastDate -> xLabelBounds.width() + Y_AXIS_LABEL_TEXT_OFFSET
                else -> xLabelBounds.width() / 2
            }

            val dayNumber = date.getDayOfMonth()

            val text = if (index == 0 || dayNumber == "01") {
                "$dayNumber ${date.getMonthName()}"
            } else {
                dayNumber
            }

            canvas.drawText(
                text,
                index * verticalsInterval - xOffset.toFloat(),
                height.toFloat(),
                xAxisLabelPaint
            )
        }
    }

    private fun drawYAxisGraphLabel(canvas: Canvas) {
        (1 until lineCount).forEach { value ->
            canvas.drawText(
                (value * valueStep.toInt()).toString(),
                width.toFloat() - Y_AXIS_LABEL_TEXT_OFFSET,
                height - value * horizontalInterval,
                yAxisLabelPaint
            )
        }
    }

    private fun drawRanges(canvas: Canvas) {
        ranges.forEach { graphItem ->
            val path = Path()
            graphItem.points.forEachIndexed { index, point ->
                val x = point.x * verticalsInterval
                val y = height - point.y * horizontalInterval - axisTextSize

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            graphLinePaint.color = graphItem.color
            canvas.drawPath(path, graphLinePaint)
        }
    }

    private fun getDateWithoutTimeFromTimestampInSeconds(timestamp: Long): Date {
        return Date(timestamp * 1000).atDayStart()
    }

    private fun calculateDaysBetweenTwoDate(firstDate: Date, lastDate: Date): Int {
        val diffInMilliseconds = lastDate.time - firstDate.time
        return (diffInMilliseconds / MILLISECONDS_IN_DAY).toInt()
    }

    private fun readAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.LineChartView, defStyleAttr) {
            minWidth = getDimensionPixelSize(
                R.styleable.LineChartView_minWidth,
                resources.getDimensionPixelSize(R.dimen.line_chart_min_width_default)
            )
            minHeight = getDimensionPixelSize(
                R.styleable.LineChartView_minHeight,
                resources.getDimensionPixelSize(R.dimen.line_chart_min_height_default)
            )
            valueStep = getFloat(
                R.styleable.LineChartView_valueStep,
                500f
            )
            axisTextSize = getDimensionPixelSize(
                R.styleable.LineChartView_axisTextSize,
                resources.getDimensionPixelSize(R.dimen.line_char_text_size_default)
            ).toFloat()
        }
    }
}

private const val DATE_LABEL_TEMPLATE = "01"
private const val MILLISECONDS_IN_DAY = 86400000
private const val Y_AXIS_LABEL_TEXT_OFFSET = 10f