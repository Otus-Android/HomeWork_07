package otus.homework.customview.line_chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.dto.PayloadDto
import java.text.SimpleDateFormat
import java.util.*

class LineChart(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val minWidth = context.resources.getDimension(R.dimen.line_graph_min_width)
    private val minHeight = context.resources.getDimension(R.dimen.line_graph_min_height)
    private var width = minWidth
    private var height = minHeight

    private val maxPrice: Int
    private val priceDelimiter = 800f
    private val priceLines: Int
    private val firstDay: Int
    private val lastDay: Int
    private val daysAmount: Int
    private val monthName: String

    private val axisPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.GRAY
        pathEffect = DashPathEffect(floatArrayOf(1f, 2f), 50f)
    }

    private val underGraphLabelPaint = Paint().apply {
        textSize = 12f
        color = Color.BLACK
    }

    private val rightSideGraphLabelPaint = Paint().apply {
        textSize = 8f
        color = Color.BLACK
        textAlign = Paint.Align.RIGHT
    }

    private val graphLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        pathEffect = CornerPathEffect(8f)
    }

    private val dateTextBounds = Rect()
    private val priceTextBounds = Rect()

    private val strokeOffset = axisPaint.strokeWidth / 2
    private var verticalsInterval = 0f
    private var horizontalInterval = 0f

    private val items: MutableList<LineChartItem> = mutableListOf()

    init {
        val json = readPayloadFromRawFiles()
        val itemsList = if (json == null) {
            emptyList()
        } else {
            parsePayloadJson(json)
        }

        val itemsByCategory = itemsList.groupBy(PayloadDto::category)
        maxPrice = itemsByCategory.maxOf { it.value.sumBy(PayloadDto::amount) }
        val realFirstDay = extractDayNumber(itemsList.minOf(PayloadDto::time))
        firstDay = if (realFirstDay >= 3) {
            realFirstDay - DAYS_BEFORE
        } else {
            1
        }
        lastDay = extractDayNumber(itemsList.maxOf(PayloadDto::time))
        monthName = extractMonthName(itemsList.firstOrNull()?.time ?: 0L)
        daysAmount = lastDay - firstDay + 1
        priceLines = (maxPrice / priceDelimiter + 1).toInt()

        underGraphLabelPaint.getTextBounds(
            DEFAULT_DATE_LABEL,
            0,
            DEFAULT_DATE_LABEL.length,
            dateTextBounds
        )

        val priceDelimiterAsString = priceDelimiter.toString()
        rightSideGraphLabelPaint.getTextBounds(
            priceDelimiterAsString,
            0,
            priceDelimiterAsString.length,
            priceTextBounds
        )
        initGraphItems(itemsByCategory)
    }

    private fun initGraphItems(itemsByCategory: Map<String, List<PayloadDto>>) {
        itemsByCategory.values.forEach { category ->
            val pointsList = mutableListOf<PointF>()
            val categoryByDayNumber = category.groupBy { extractDayNumber(it.time) }
            (firstDay..lastDay).forEach { day ->
                val currentDayCategory = categoryByDayNumber[day]
                if (currentDayCategory == null) {
                    pointsList.add(
                        PointF((day - firstDay).toFloat(), 0f)
                    )
                } else {
                    pointsList.add(
                        PointF(
                            (day - firstDay).toFloat(),
                            currentDayCategory.sumOf { it.amount } / priceDelimiter)
                    )
                }
            }
            items.add(LineChartItem(pointsList, getColor()))
        }
    }

    private fun getColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        val width = calculateMeasureSize(widthMode, widthSize, minWidth.toInt())
        val height = calculateMeasureSize(heightMode, heightSize, minHeight.toInt())

        this.width = width.toFloat()
        this.height = height.toFloat()

        verticalsInterval = (width - strokeOffset * 2) / (daysAmount - 1)
        horizontalInterval =
            (height - GRAPH_LABEL_HORIZONTAL_OFFSET - strokeOffset * 2) / priceLines.toFloat()

        setMeasuredDimension(width, height)
    }

    private fun calculateMeasureSize(mode: Int, size: Int, minSize: Int): Int {
        return when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)
            else -> throw IllegalStateException("Invalid measure mode")
        }
    }

    private fun readPayloadFromRawFiles(): String? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.payload)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("LineChart", "Error reading payload from raw files", e)
            null
        }
    }

    private fun parsePayloadJson(json: String): List<PayloadDto> {
        return try {
            Gson().fromJson(
                json,
                object : TypeToken<List<PayloadDto>>() {}.type
            )
        } catch (e: Exception) {
            Log.e("LineChart", "Error parsing payload", e)
            emptyList()
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawAxis(canvas)
        drawLabels(canvas)
        drawLines(canvas)
    }

    private fun drawAxis(canvas: Canvas) {
        (0 until daysAmount).forEach { day ->
            val xVertical = day * verticalsInterval + strokeOffset
            canvas.drawLine(
                xVertical,
                0f,
                xVertical,
                height - GRAPH_LABEL_HORIZONTAL_OFFSET,
                axisPaint
            )
        }

        (0..priceLines).forEach { price ->
            val yHorizontal = price * horizontalInterval
            canvas.drawLine(
                0f,
                yHorizontal,
                width,
                yHorizontal,
                axisPaint
            )
        }
    }

    private fun drawLabels(canvas: Canvas) {
        var curGraphVerticalPlace = 0
        (firstDay..lastDay).forEach { dayNumber ->
            val offset = when (dayNumber) {
                firstDay -> {
                    0
                }
                lastDay -> {
                    dateTextBounds.width()
                }
                else -> {
                    dateTextBounds.width() / 2
                }
            }

            val fullDayNumber = if (dayNumber == firstDay) {
                "${dayNumber.toStringWithLeadingZero()} $monthName"
            } else {
                dayNumber.toStringWithLeadingZero()
            }

            canvas.drawText(
                fullDayNumber,
                curGraphVerticalPlace * verticalsInterval - offset,
                height,
                underGraphLabelPaint
            )
            curGraphVerticalPlace++
        }

        (1 until priceLines).forEach { price ->
            canvas.drawText(
                (price * priceDelimiter.toInt()).toString(),
                width,
                height - price * horizontalInterval - GRAPH_LABEL_HORIZONTAL_OFFSET + priceTextBounds.height(),
                rightSideGraphLabelPaint
            )
        }
    }

    private fun drawLines(canvas: Canvas) {
        items.forEach { graphItem ->
            val path = Path()
            graphItem.points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(
                        point.x * verticalsInterval,
                        height - point.y * horizontalInterval - GRAPH_LABEL_HORIZONTAL_OFFSET
                    )
                } else {
                    path.lineTo(
                        point.x * verticalsInterval,
                        height - point.y * horizontalInterval - GRAPH_LABEL_HORIZONTAL_OFFSET
                    )
                }
            }
            graphLinePaint.color = graphItem.color
            canvas.drawPath(path, graphLinePaint)
        }
    }


    private fun Int.toStringWithLeadingZero(): String {
        if (this < 10) {
            return "0$this"
        }
        return this.toString()
    }

    private fun extractMonthName(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time * 1000
        return SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    }

    private fun extractDayNumber(time: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time * 1000
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    companion object {
        private const val DAYS_BEFORE = 2
        private const val GRAPH_LABEL_HORIZONTAL_OFFSET = 12f
        private const val DEFAULT_DATE_LABEL = "01"
    }
}