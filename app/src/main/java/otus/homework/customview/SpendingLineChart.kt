package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.os.bundleOf
import java.util.*
import kotlin.math.max

class SpendingLineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val DAYS_BEFORE_MIN_DAY = 2
        const val LABEL_ABSCISSA_HEIGHT = 40f
        const val DEFAULT_DATE_LABEL = "01"

        const val POINTS_KEY = "points_key"
        const val MAX_AMOUNT_KEY = "max_amount_key"
        const val MIN_DAY_KEY = "min_day_key"
        const val MAX_DAY_KEY = "max_day_key"
        const val DAYS_AMOUNT_KEY = "days_amount_key"
        const val AMOUNT_LINES_COUNT_KEY = "amount_lines_count_key"
        const val SUPER_PARCELABLE_KEY = "super_parcelable"
    }

    private val minWidth = context.resources.getDimension(R.dimen.line_chart_min_width)
    private val minHeight = context.resources.getDimension(R.dimen.line_chart_min_height)
    private var width = minWidth
    private var height = minHeight

    private var points: List<PointF> = listOf(
        PointF(5f, 400f),
        PointF(10f, 100f),
        PointF(15f, 1600f),
        PointF(20f, 300f),
        PointF(25f, 200f),
    )

    private var maxAmount: Int = 1600
    private var minDay: Int = 5
    private var maxDay: Int = 25
    private var daysAmount: Int = 21

    private val amountDelimiter = 800f
    private var amountLinesCount: Int = (maxAmount / amountDelimiter + 1).ceil()

    private val axisPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.GRAY
        pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 50f)
    }

    private val abscissaLabelPaint = Paint().apply {
        textSize = LABEL_ABSCISSA_HEIGHT
        color = Color.BLACK
    }

    private val ordinateLabelPaint = Paint().apply {
        textSize = abscissaLabelPaint.textSize
        color = Color.BLACK
        textAlign = Paint.Align.RIGHT
    }

    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.RED
        pathEffect = CornerPathEffect(8f)
    }

    private val dateTextBounds = Rect().apply {
        abscissaLabelPaint.getTextBounds(
            DEFAULT_DATE_LABEL,
            0,
            DEFAULT_DATE_LABEL.length,
            this
        )
    }

    private val amountTextBounds = Rect().apply {
        ordinateLabelPaint.getTextBounds(
            amountDelimiter.toString(),
            0,
            amountDelimiter.toString().length,
            this
        )
    }

    private val strokeOffset = axisPaint.strokeWidth / 2
    private var verticalsInterval = 0f
    private var horizontalInterval = 0f

    fun setExpenses(expenses: Expenses, category: String) {
        val expensesByCategory = expenses.filter { it.category == category }
        maxAmount = expensesByCategory.maxOf(Spending::amount)

        val realMinDay = expensesByCategory.minOf(Spending::time).toDayOfMonth()
        minDay = (realMinDay - DAYS_BEFORE_MIN_DAY).takeIf { realMinDay < 3 } ?: 1
        maxDay = expensesByCategory.maxOf(Spending::time).toDayOfMonth()
        daysAmount = maxDay - minDay + 1
        amountLinesCount = (maxAmount / amountDelimiter + 1).ceil()
        points = initPoints(expensesByCategory)

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = calculateMeasureSize(widthMode, widthSize, minWidth.ceil())
        val height = calculateMeasureSize(heightMode, heightSize, minHeight.ceil())

        this.width = width.toFloat()
        this.height = height.toFloat()

        verticalsInterval = (width - strokeOffset * 2) / (daysAmount - 1)
        horizontalInterval = (height - LABEL_ABSCISSA_HEIGHT - strokeOffset * 2) / amountLinesCount.toFloat()

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawAxes(canvas)
        drawLabels(canvas)
        drawLine(canvas)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = bundleOf(
            MAX_AMOUNT_KEY to maxAmount,
            MIN_DAY_KEY to minDay,
            MAX_DAY_KEY to maxDay,
            DAYS_AMOUNT_KEY to daysAmount,
            AMOUNT_LINES_COUNT_KEY to amountLinesCount,
            POINTS_KEY to points,
        )

        bundle.putParcelable(SUPER_PARCELABLE_KEY, super.onSaveInstanceState())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            maxAmount = state.getInt(MAX_AMOUNT_KEY)
            minDay = state.getInt(MIN_DAY_KEY)
            maxDay = state.getInt(MAX_DAY_KEY)
            daysAmount = state.getInt(DAYS_AMOUNT_KEY)
            amountLinesCount = state.getInt(AMOUNT_LINES_COUNT_KEY)

            points = state.getParcelableArrayList<PointF>(POINTS_KEY)?.toList() ?: emptyList()

            super.onRestoreInstanceState(state.getParcelable(SUPER_PARCELABLE_KEY))
        }
    }

    private fun initPoints(expenses: List<Spending>) = mutableListOf<PointF>().apply {
        val expensesByDayNumber = expenses.groupBy { it.time.toDayOfMonth() }

        for (day in minDay..maxDay) {
            val currentDay = (day - minDay).toFloat()
            val currentDayAmount = expensesByDayNumber[day]
                ?.sumOf { it.amount }
                ?.div(amountDelimiter) ?: 0f

            add(PointF(currentDay, currentDayAmount))
        }
    }

    private fun calculateMeasureSize(mode: Int, size: Int, minSize: Int): Int {
        return when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> max(minSize, size)
            else -> throw IllegalStateException("Invalid measure mode")
        }
    }

    private fun drawAxes(canvas: Canvas) {
        for (day in 0 until daysAmount) {
            val xVertical = day * verticalsInterval + strokeOffset
            canvas.drawLine(
                xVertical,
                0f,
                xVertical,
                height - LABEL_ABSCISSA_HEIGHT,
                axisPaint
            )
        }

        for (amount in 0..amountLinesCount) {
            val yHorizontal = amount * horizontalInterval
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
        for ((index, dayNumber) in (minDay..maxDay).withIndex()) {
            val offset = when (dayNumber) {
                minDay -> 0
                maxDay -> dateTextBounds.width()
                else -> dateTextBounds.width() / 2
            }

            canvas.drawText(
                dayNumber.toStringWithLeadingZero(),
                index * verticalsInterval - offset,
                height,
                abscissaLabelPaint
            )
        }

        for (amount in 1 until amountLinesCount) {
            canvas.drawText(
                (amount * amountDelimiter.ceil()).toString(),
                width,
                height - amount * horizontalInterval - LABEL_ABSCISSA_HEIGHT + amountTextBounds.height(),
                ordinateLabelPaint
            )
        }
    }

    private fun drawLine(canvas: Canvas) {
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = point.x * verticalsInterval
            val y = height - point.y * horizontalInterval - LABEL_ABSCISSA_HEIGHT

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)
    }

    private fun Long.toDayOfMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun Int.toStringWithLeadingZero(): String {
        if (this < 10) {
            return "0$this"
        }
        return toString()
    }
}