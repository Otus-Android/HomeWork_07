package otus.homework.customview.task_2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import otus.homework.customview.R
import otus.homework.customview.task_1.model.Category
import otus.homework.customview.task_1.model.Data
import otus.homework.customview.util.px
import java.util.*

class CategoryChartBogdan @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private lateinit var chartPaint: Paint
    private val axisPaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 3.toFloat()
    }

    private val pointAxisPaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        strokeWidth = 7.toFloat()
    }

    private lateinit var chartPointPaint: Paint

    private val textPaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        strokeWidth = 1.toFloat()
        textSize = 35.toFloat()
    }

    private val textAxisPaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1.toFloat()
        textSize = TEXT_AXIS_PAINT_SIZE
        textAlign = Paint.Align.CENTER
    }

    private val path = Path()
    private var data = listOf<Data>()
    private val calendar = Calendar.getInstance()
    private var category: Category? = null
    private var month: Int = -1
    private var axisStep = 0f
    private var amountStep = 0f
    private var lengthInOnePieceOfAmount = 0f
    private var stepAxisForText = 0
    private var amountMaxValue = 0
    private var widthForLeftSide = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.CategoryChartBogdan) {
            val color =
                getColor(R.styleable.CategoryChartBogdan_chartColor, Color.MAGENTA)
            val width = getDimension(R.styleable.CategoryChartBogdan_chartWidth, 1.px.toFloat())
            setupChartPaint(
                mColor = color,
                mWidth = width,
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                if (wSize < hSize) {
                    setMeasuredDimension(wSize, wSize)
                } else {
                    setMeasuredDimension(wSize, hSize)
                }
            }

            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(UNSPECIFIED_SIZE, UNSPECIFIED_SIZE)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawAxis(it, data)
        }
    }

    private fun drawAxis(canvas: Canvas, data: List<Data>) {
        val height = if (width < height) width else height
        canvas.drawLine(
            widthForLeftSide,
            height - (2 * TEXT_AXIS_PAINT_SIZE),
            width - (2 * TEXT_AXIS_PAINT_SIZE),
            height - (2 * TEXT_AXIS_PAINT_SIZE),
            axisPaint
        )

        canvas.drawLine(
            widthForLeftSide,
            (2 * TEXT_AXIS_PAINT_SIZE),
            widthForLeftSide + TEXT_AXIS_PAINT_SIZE,
            (2 * TEXT_AXIS_PAINT_SIZE) + (2 * TEXT_AXIS_PAINT_SIZE),
            axisPaint
        )

        canvas.drawLine(
            widthForLeftSide,
            (2 * TEXT_AXIS_PAINT_SIZE),
            widthForLeftSide - TEXT_AXIS_PAINT_SIZE,
            (2 * TEXT_AXIS_PAINT_SIZE) + (2 * TEXT_AXIS_PAINT_SIZE),
            axisPaint
        )

        canvas.drawLine(
            widthForLeftSide,
            (2 * TEXT_AXIS_PAINT_SIZE),
            widthForLeftSide,
            height - (2 * TEXT_AXIS_PAINT_SIZE),
            axisPaint
        )

        canvas.drawLine(
            width - (2 * TEXT_AXIS_PAINT_SIZE),
            height - (2 * TEXT_AXIS_PAINT_SIZE),
            width - (2 * TEXT_AXIS_PAINT_SIZE) - (2 * TEXT_AXIS_PAINT_SIZE),
            height - (2 * TEXT_AXIS_PAINT_SIZE) - TEXT_AXIS_PAINT_SIZE,
            axisPaint
        )

        canvas.drawLine(
            width - (2 * TEXT_AXIS_PAINT_SIZE),
            height - (2 * TEXT_AXIS_PAINT_SIZE),
            width - (2 * TEXT_AXIS_PAINT_SIZE) - (2 * TEXT_AXIS_PAINT_SIZE),
            height - (2 * TEXT_AXIS_PAINT_SIZE) + TEXT_AXIS_PAINT_SIZE,
            axisPaint
        )

        val textSizeMonth = textPaint.measureText(month.getMonth())
        textPaint.textAlign = Paint.Align.LEFT



        canvas.drawText(
            month.getMonth(),
            width - (2 * TEXT_AXIS_PAINT_SIZE) - textSizeMonth,
            height.toFloat() - (4 * TEXT_AXIS_PAINT_SIZE),
            textPaint
        )
        canvas.drawText(
            category?.categoryName ?: "Unknown",
            (widthForLeftSide * 2),
            (4 * TEXT_AXIS_PAINT_SIZE),
            textPaint
        )
        if (data.isNotEmpty()) {
            drawPoints(canvas, height)
            drawChart(
                canvas = canvas,
                data = data,
                height = height
            )
        }
    }

    private fun drawPoints(canvas: Canvas, height: Int) {
        val length = width - widthForLeftSide - (2 * TEXT_AXIS_PAINT_SIZE)
        axisStep = getMonthAxisStep(length)
        var tempLength = widthForLeftSide
        var counter = 1
        while (tempLength <= length + axisStep) {
            canvas.drawPoint(tempLength, height - (2 * TEXT_AXIS_PAINT_SIZE), pointAxisPaint)
            if (counter == 1 || counter % 5 == 0) {
                canvas.drawText(counter.toString(), tempLength, height - TEXT_AXIS_PAINT_SIZE, textAxisPaint)
            }
            tempLength += axisStep
            counter++
        }
        val tempHeight = height - (2 * TEXT_AXIS_PAINT_SIZE) - (4 * TEXT_AXIS_PAINT_SIZE)
        amountStep = tempHeight / 6
        var amountCounter = height - (2 * TEXT_AXIS_PAINT_SIZE) - amountStep
        var tempCounter = stepAxisForText
        while (amountCounter >= (2 * TEXT_AXIS_PAINT_SIZE) + (2 * TEXT_AXIS_PAINT_SIZE) + (4 * TEXT_AXIS_PAINT_SIZE)) {
            canvas.drawPoint(widthForLeftSide, amountCounter, pointAxisPaint)
            canvas.drawText(
                "$tempCounter",
                widthForLeftSide - 2 * TEXT_AXIS_PAINT_SIZE,
                amountCounter + TEXT_AXIS_PAINT_SIZE * 0.3f,
                textAxisPaint
            )
            tempCounter += stepAxisForText
            amountCounter -= amountStep
        }
        lengthInOnePieceOfAmount = amountStep * 5 / data.maxOf { it.amount }
    }

    private fun getMonthAxisStep(width: Float): Float {
        calendar.time = data[0].time
        return width / (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + 1)
    }

    private fun drawChart(canvas: Canvas, data: List<Data>, height: Int) {
        path.reset()
        path.moveTo(widthForLeftSide, height - (2 * TEXT_AXIS_PAINT_SIZE))
        data.forEach {
            calendar.clear()
            calendar.time = it.time
            path.lineTo(
                (widthForLeftSide) + calendar.get(Calendar.DAY_OF_MONTH) * axisStep,
                height - (2 * TEXT_AXIS_PAINT_SIZE) - (it.amount * lengthInOnePieceOfAmount),
            )
            canvas.drawPoint(
                (widthForLeftSide) + calendar.get(Calendar.DAY_OF_MONTH) * axisStep,
                height - (2 * TEXT_AXIS_PAINT_SIZE) - (it.amount * lengthInOnePieceOfAmount),
                chartPointPaint
            )
        }
        chartPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        canvas.drawPath(path, chartPaint)
    }

    fun setData(category: Category, data: List<Data>, month: Int) {
        this.category = category
        this.month = month
        this.data = data.filter {
            calendar.time = it.time
            it.category == category && calendar.get(Calendar.MONTH) == month
        }
        amountMaxValue = this.data.maxOf { it.amount }
        stepAxisForText = amountMaxValue / 5
        widthForLeftSide = textAxisPaint.measureText(amountMaxValue.toString()) + (2 * TEXT_AXIS_PAINT_SIZE)
        requestLayout()
        invalidate()
    }

    private fun setupChartPaint(mColor: Int, mWidth: Float) {
        chartPaint = Paint().apply {
            color = mColor
            style = Paint.Style.STROKE
            strokeWidth = mWidth
        }

        chartPointPaint = Paint().apply {
            color = mColor
            style = Paint.Style.FILL
            strokeWidth = 10.toFloat()
        }
    }

    private fun Int.getMonth(): String = when (this) {
        Calendar.JANUARY -> "Январь"
        Calendar.FEBRUARY -> "Февраль"
        Calendar.MARCH -> "Март"
        Calendar.APRIL -> "Апрель"
        Calendar.MAY -> "Май"
        Calendar.JUNE -> "Июнь"
        Calendar.JULY -> "Июль"
        Calendar.AUGUST -> "Август"
        Calendar.SEPTEMBER -> "Сентябрь"
        Calendar.OCTOBER -> "Октябрь"
        Calendar.NOVEMBER -> "Ноябрь"
        Calendar.DECEMBER -> "Декабрь"
        else -> "Unknown"
    }

    companion object {
        private const val UNSPECIFIED_SIZE = 1500
        private const val TEXT_AXIS_PAINT_SIZE = 20f
    }
}