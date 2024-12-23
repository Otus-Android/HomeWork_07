package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

class LineChartView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val data: MutableMap<String, MutableMap<Long, Float>> = mutableMapOf()
    private val secsInDay: Long = 60 * 60 * 24
    private var minDay: Long = 0
    private var maxDay: Long = 0
    private var maxValue = 0f
    private var catCount = 0
    private var colWidth = 0      // in pixels
    private var dayWidth = 0      // in pixels
    private var winWidth = 1000   // in pixels
    private var winHeight = 1000  // in pixels
    private var baseLine = 950    // in pixels
    private var chartWidth = 800  // in pixels
    private var chartHeight = 900 // in pixels
    private val vertMargin = 50   // in pixels
    private val leftMargin = 100  // in pixels
    private lateinit var palette : Palette

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> winWidth = wSize
            MeasureSpec.AT_MOST -> winWidth = min(winWidth, wSize)
            MeasureSpec.UNSPECIFIED -> winWidth = wSize
        }
        when (hMode) {
            MeasureSpec.EXACTLY -> winHeight = hSize
            MeasureSpec.AT_MOST -> winHeight = min(winHeight, hSize)
            MeasureSpec.UNSPECIFIED -> winHeight = hSize
        }

        setMeasuredDimension(winWidth, winHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        baseLine = winHeight - vertMargin
        chartWidth = winWidth - leftMargin * 2
        chartHeight = baseLine - vertMargin * 2
        dayWidth = chartWidth / (maxDay - minDay + 1).toInt()
        colWidth = dayWidth / (catCount + 1)
        // TODO: check colWidth > 0

        drawAxis(canvas)
        drawData(canvas)
    }

    private fun drawAxis(canvas: Canvas) {
        val linePaint = Paint().apply { color = Color.BLACK }
        val textPaint = Paint().apply { textSize = 32f }
        var markY = 1f
        while (markY*10f <= maxValue) markY *= 10f
        var y = (baseLine - vertMargin).toFloat() * markY / maxValue
        // draw Y line at x=leftMargin with top vertical arrow
        canvas.drawLine(leftMargin.toFloat(), (vertMargin-20).toFloat(), leftMargin.toFloat(), (baseLine+22).toFloat(), linePaint)
        canvas.drawLine(leftMargin.toFloat(), (vertMargin-20).toFloat(), (leftMargin-6).toFloat(), vertMargin.toFloat(), linePaint)
        canvas.drawLine(leftMargin.toFloat(), (vertMargin-20).toFloat(), (leftMargin+6).toFloat(), vertMargin.toFloat(), linePaint)
        // draw origin and scale marks
        canvas.drawLine((leftMargin-20).toFloat(), y, leftMargin.toFloat(), y, linePaint)
        canvas.drawText("0", (leftMargin-32).toFloat(), (baseLine-8).toFloat(), textPaint)
        canvas.drawText(String.format("%4.0f", markY),0f,y-8, textPaint)
        // draw X line at y=baseLine with right horizontal arrow
        val endX = (chartWidth+leftMargin+24).toFloat()
        canvas.drawLine((leftMargin-20).toFloat(), baseLine.toFloat(), endX, baseLine.toFloat(), linePaint)
        canvas.drawLine((chartWidth+leftMargin+4).toFloat(), (baseLine-6).toFloat(), endX, baseLine.toFloat(), linePaint)
        canvas.drawLine((chartWidth+leftMargin+4).toFloat(), (baseLine+6).toFloat(), endX, baseLine.toFloat(), linePaint)
        // draw day marks
        textPaint.apply { textSize = 40f }
        y = (winHeight-4).toFloat()
        for (day in minDay .. maxDay) {
            val date = LocalDateTime.ofEpochSecond(day * secsInDay, 0, ZoneOffset.UTC).toLocalDate()
            val text = String.format("%02d.%02d.%04d", date.dayOfMonth, date.month.value, date.year)
            val x = ((day - minDay) * dayWidth + leftMargin).toFloat()
            canvas.drawLine(x + dayWidth, baseLine.toFloat(), x + dayWidth, (baseLine+22).toFloat(), linePaint)
            canvas.drawText(text, x + 8, y, textPaint)
        }
    }

    private fun drawData(canvas: Canvas) {
        var index = 0
        val paint = Paint()
        for (cat in data) {
            paint.apply {
                color = palette.getColor(cat.key)
            }
            for (day in cat.value) {
                val x = ((day.key - minDay) * dayWidth + index * colWidth).toInt() + leftMargin
                val y = ((baseLine - vertMargin).toFloat() * day.value / maxValue).toInt()
                val r = Rect(x + 1,baseLine - y,x + colWidth, baseLine - 1)
                canvas.drawRect(r, paint)
            }
            ++index
        }
    }

    fun setContent(products: List<ProductData>, palette : Palette) {
        this.palette = palette
        data.clear()
        maxValue = 0f
        val sorted = products.sortedBy { it.time }
        minDay = sorted.first().time / secsInDay
        maxDay = sorted.last().time / secsInDay
        val catMap = sorted.groupBy { it.category }
        for (cat in catMap) {
            data.put(cat.key, mutableMapOf())
            val dayMap = cat.value.groupBy { it.time / secsInDay }
            for (day in dayMap) {
                data[cat.key]?.put(day.key, day.value.sumOf { it.amount }.toFloat())
            }
            var amount = data[cat.key]?.values?.max()
            if (amount != null) {
                if (amount > maxValue) maxValue = amount
            }
        }
        catCount = data.keys.size
    }
}
