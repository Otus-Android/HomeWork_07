package otus.homework.customview.line_chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import otus.homework.customview.pie_chart.CustomPieChart
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class CustomLineChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var data: LineData? = null

    private val axisPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.GRAY
        strokeWidth = 3F
    }
    private val axisTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        textAlign = Paint.Align.LEFT
    }
    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(40f, 20f), 0f)
        isAntiAlias = true
        color = Color.LTGRAY
        strokeWidth = 1.5F
    }
    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = LINE_COLOR
        strokeWidth = 6F
    }

    private val path = Path()

    private var startX = 0f
    private var startY = 0f

    private val axisPadding = 32f
    private val textPadding = 8f

    private var xGridSpace = 0f
    private var yGridSpace = 0f
    private var xAxisOrder = 0
    private var yAxisOrder = 0

    private var xAxisStep = 1L
    private var yAxisStep = 1
    private var startDate = System.currentTimeMillis() / 1000

    private var animator: ValueAnimator? = null
    private var currentPointIdx = -1

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> maxOf(minimumWidth, widthSize)
            MeasureSpec.AT_MOST -> minOf(minimumWidth, maxOf(minimumWidth, widthSize))
            else -> minimumWidth
        }

        val chartHeight = CustomPieChart.calculateHeight(width)
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> maxOf(chartHeight, heightSize)
            MeasureSpec.AT_MOST -> minOf(chartHeight, maxOf(chartHeight, heightSize))
            else -> chartHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun getMinimumHeight(): Int {
        return MIN_CHART_HEIGHT
    }

    override fun getMinimumWidth(): Int {
        return MIN_CHART_WIDTH
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setChartSizes()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        data?.values?.let {
            drawAxis(canvas)
            drawGridLines(canvas)
            drawMainLine(canvas)
        }
    }

    fun setData(data: LineData) {
        this.data = data.apply {
            sortedValues = data.values.sortedBy { it.time }
        }
        setAxisOrder()
        startInitAnimation()
    }

    private fun setAxisOrder() {
        xAxisOrder = 1
        yAxisOrder = 1

        var sortedData = data?.values?.sortedByDescending { it.amount }
        if (!sortedData.isNullOrEmpty()) {
            val maxValue = sortedData.first().amount
            var remainder = maxValue / 10
            while (remainder != 0) {
                yAxisOrder *= 10
                remainder /= 10
            }
            yAxisStep = 10 / COUNT_GRID_LINE * yAxisOrder
        }

        sortedData = data?.values?.sortedBy { it.time }
        if (!sortedData.isNullOrEmpty()) {
            val minDate = sortedData.first().time
            val maxDate = sortedData.last().time
            val countDays = TimeUnit.SECONDS.toDays(maxDate - minDate)
            xAxisOrder = maxOf(countDays.toInt() / COUNT_GRID_LINE, 1)
            xAxisStep = TimeUnit.DAYS.toSeconds(xAxisOrder.toLong())
            startDate = minDate.toStartOfDay()
        }
    }

    private fun startInitAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, data?.sortedValues?.size ?: 0).apply {
            duration = 500
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                currentPointIdx = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator?.start()
    }

    private fun drawAxis(canvas: Canvas) {
        //y axis
        canvas.drawLine(2 * axisPadding, axisPadding, 2 * axisPadding, height - axisPadding, axisPaint)
        //x axis
        canvas.drawLine(axisPadding, height - 2 * axisPadding, width - axisPadding, height - 2 * axisPadding, axisPaint)
    }

    private fun drawGridLines(canvas: Canvas) {
        // vertical lines
        var startX = 2 * axisPadding + xGridSpace
        val startY = axisPadding
        var stopY = height - 2 * axisPadding
        for (i in 0 until COUNT_GRID_LINE) {
            canvas.drawLine(startX, startY, startX, stopY, gridPaint)
            val axisText = (startDate + TimeUnit.DAYS.toSeconds((i * xAxisOrder).toLong())).toDate()
            val widthText = axisTextPaint.measureText(axisText)
            canvas.drawText(
                axisText,
                startX - widthText / 2,
                height - axisPadding,
                axisTextPaint
            )
            startX += xGridSpace
        }

        // horizontal lines
        startX = 2 * axisPadding
        stopY -= yGridSpace
        for (i in 1..COUNT_GRID_LINE) {
            canvas.drawLine(startX, stopY, width - axisPadding, stopY, gridPaint)
            val widthText = axisTextPaint.measureText("${10 * yAxisOrder} Р") + textPadding
            canvas.drawText(
                "${(10 / COUNT_GRID_LINE) * i * yAxisOrder} Р",
                width - widthText - axisPadding,
                stopY - textPadding,
                axisTextPaint
            )
            stopY -= yGridSpace
        }
    }

    private fun drawMainLine(canvas: Canvas) {
        val sortedData = data?.sortedValues
        if (!sortedData.isNullOrEmpty()) {
            path.reset()

            path.moveTo(startX, startY)
            sortedData.forEachIndexed { i, category ->
                val x = (category.time - startDate) * xGridSpace / xAxisStep
                val y = (category.amount * yGridSpace) / yAxisStep

                if (i == 0) {
                    path.moveTo(x + startX, startY - y)
                    canvas.drawCircle(x + startX, startY - y, 4f, linePaint)
                } else if (currentPointIdx >= i) {
                    path.lineTo(x + startX, startY - y)
                    canvas.drawCircle(x + startX, startY - y, 4f, linePaint)
                }
            }

            canvas.drawPath(path, linePaint)
        }
    }

    private fun setChartSizes() {
        startX = 2 * axisPadding
        startY = height - 2 * axisPadding
        // 2 for padding
        xGridSpace = width / (COUNT_GRID_LINE + 2f)
        yGridSpace = height / (COUNT_GRID_LINE + 2f)
        axisTextPaint.textSize = height / 32f
    }

    private fun Long.toStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        return calendar.timeInMillis / 1000
    }

    private fun Long.toDate(): String {
        val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return formatter.format(Date(this * 1000))
    }

    companion object {
        private const val MIN_CHART_WIDTH = 800
        private const val ASPECT_RATIO_COEFFICIENT = 0.6
        private const val MIN_CHART_HEIGHT = (ASPECT_RATIO_COEFFICIENT * MIN_CHART_WIDTH).toInt()

        const val LINE_COLOR = Color.BLUE
        const val COUNT_GRID_LINE = 5

        private const val DATE_FORMAT = "dd MMM"
    }
}