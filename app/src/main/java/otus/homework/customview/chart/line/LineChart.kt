package otus.homework.customview.chart.line

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.PayloadItem
import otus.homework.customview.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class LineChart(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val minWidth = 400f
    private val minHeight = 200f

    private var width = minWidth
    private var height = minHeight

    private var countColumns = 0
    private val countRows = 10
    private val offsetBeforeFirstDate = 1
    private val offsetAfterLastDate = 2

    private var stepColumn = 0f
    private var stepRow = 0f
    private var maxPrice = 0
    private var firstDay = 0L

    private val lineChartItem = mutableListOf<LineChartItem>()

    private val axisPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.GRAY
        pathEffect = DashPathEffect(floatArrayOf(5f, 10f), 0f)
    }

    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        pathEffect = CornerPathEffect(8f)
    }

    private val priceLabels = Paint().apply {
        textSize = 20f
        color = Color.BLACK
        textAlign = Paint.Align.RIGHT
    }

    private val dateLabels = Paint().apply {
        textSize = 20f
        color = Color.BLACK
        textAlign = Paint.Align.LEFT
    }

    private val strokeOffset = axisPaint.strokeWidth / 2

    init {
        val payloads = getPayloads()
        val categories = payloads.groupBy(PayloadItem::category)
        maxPrice = payloads.maxOf(PayloadItem::amount)
        firstDay = payloads.minOf(PayloadItem::time)
        categories.entries.forEach { category ->
            val points = mutableListOf<PointF>()
            val categoryByDays = category.value.groupBy { it.numberDay }
            categoryByDays.entries.forEachIndexed { index, entry ->
                val offsetStartColum = entry.key + offsetBeforeFirstDate
                if (index == 0) {
                    val firstDay = if (offsetStartColum == 1) 0f
                    else offsetStartColum - 1
                    points.add(PointF(firstDay.toFloat(), 0f))
                }
                val sum = entry.value.sumBy { it.amount }
                points.add(PointF(offsetStartColum.toFloat(), sum.toFloat()))
            }

            points.getOrNull(points.lastIndex)?.let {
                points.add(PointF(it.x + 1, 0f))
            }

            lineChartItem.add(
                LineChartItem(
                    categoryName = category.key,
                    points = points,
                    color = getColor()
                )
            )
        }
        val countDates = payloads.groupBy(PayloadItem::date)
        countColumns = countDates.size + offsetBeforeFirstDate + offsetAfterLastDate
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = calcMeasureSize(widthMode, widthSize, minWidth.toInt())
        val height = calcMeasureSize(heightMode, heightSize, minHeight.toInt()) - OFFSET_LABELS

        this.width = width.toFloat()
        this.height = height.toFloat()

        stepColumn = (width / countColumns).toFloat()
        stepRow = (height / countRows).toFloat()

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawAxis(canvas)
        drawLines(canvas)
        drawLabels(canvas)
    }

    private fun drawLabels(canvas: Canvas?) {
        val dt = (maxPrice + (maxPrice * 0.1f)) / countRows
        (0..countRows).reversed().forEachIndexed { index, value ->
            canvas?.drawText(
                (dt * value).toString(),
                width - 5f,
                index * stepRow - 5,
                priceLabels
            )
        }

        (0..countColumns).forEachIndexed { index, value ->
            canvas?.drawText(
                convertLongToTime(firstDay + (index * 24 * 60 * 60)),
                index * stepColumn,
                height + OFFSET_LABELS,
                dateLabels
            )
        }
    }

    private fun drawAxis(canvas: Canvas?) {
        (0 until countColumns + 1).forEachIndexed { index, column ->
            val borderOffset =
                if (index == 0) strokeOffset else if (index == column) -strokeOffset else 0
            val pointX = column * stepColumn + borderOffset.toFloat()
            canvas?.drawLine(
                pointX,
                0f,
                pointX,
                height,
                axisPaint
            )
        }

        (0 until countRows + 1).forEachIndexed { index, row ->
            val borderOffset =
                if (index == 0) strokeOffset else if (index == row) -strokeOffset else 0
            val pointY = row * stepRow + borderOffset.toFloat()
            canvas?.drawLine(
                0f,
                pointY,
                width,
                pointY,
                axisPaint
            )
        }
    }

    private fun drawLines(canvas: Canvas?) {
        lineChartItem.forEach { item ->
            val path = Path()
            item.points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(
                        point.x * stepColumn,
                        height - point.y * stepRow
                    )
                } else {

                    path.lineTo(
                        point.x * stepColumn - linePaint.strokeWidth / 4,
                        height - (height * point.y) / (maxPrice + (maxPrice * 0.1f))
                    )
                }
            }
            linePaint.color = item.color
            canvas?.drawPath(path, linePaint)
        }
    }

    private fun getPayloads(): List<PayloadItem> = try {
        val json = context.resources.openRawResource(R.raw.payload).bufferedReader()
            .use { it.readText() }
        val list: List<PayloadItem> =
            Gson().fromJson(json, object : TypeToken<List<PayloadItem>>() {}.type)
        var startDate = ""
        list.sortedBy { it.time }.mapIndexed { index, payloadItem ->
            val date = convertLongToTime(payloadItem.time)
            val numberDay = if (index == 0) {
                startDate = date
                1
            } else {
                getCountDaysBetweenDates(startDate, date)
            }
            payloadItem.copy(date = date, numberDay = numberDay)
        }
    } catch (e: Exception) {
        throw e
    }

    private fun calcMeasureSize(measureSpec: Int, size: Int, minSize: Int): Int =
        when (measureSpec) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)
            else -> throw java.lang.Exception("not define")
        }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time * 1000)
        val format = SimpleDateFormat(DATE_PATTERN)
        return format.format(date)
    }

    private fun getCountDaysBetweenDates(startDate: String, endDate: String): Int {
        val dateFormat = SimpleDateFormat(DATE_PATTERN)
        val milliseconds =
            (dateFormat.parse(endDate)?.time ?: 0L) - (dateFormat.parse(startDate)?.time ?: 0L)
        return (milliseconds / (24 * 60 * 60 * 1000)).toInt() + 1
    }

    private fun getColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    companion object {
        const val DATE_PATTERN = "dd.MM.yyyy"
        const val OFFSET_LABELS = 20
    }
}