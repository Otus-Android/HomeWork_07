package otus.homework.customview.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.models.Category
import otus.homework.customview.models.Expense
import otus.homework.customview.utils.areDatesOnSameDay
import otus.homework.customview.utils.convertToDp
import otus.homework.customview.utils.convertUnixTimestampToDate
import otus.homework.customview.utils.generateRandomColor
import otus.homework.customview.utils.getNextDay
import otus.homework.customview.utils.spToPx
import java.util.Date
import java.util.TreeSet
import kotlin.math.ceil
import kotlin.math.max

class LinearView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val gridPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val brokenLinePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = spToPx(12f)
    }

    private val path = Path()

    private var categories: Map<String, Category> = mapOf()
    private var sumExpense: Int = 0
    private var categoryToSum: Map<String, Int> = mapOf()
    private var colors = mapOf<String, Int>()
    private var expensesByDays: Map<String, List<Int>> = mapOf()
    private var uniqueDays: TreeSet<Date> =
        TreeSet { o1, o2 -> if (areDatesOnSameDay(o1, o2)) 0 else o1.compareTo(o2) }
    private var widthSize = 0f
    private var heightSize = 0f
    private var startX = 0f
    private var startY = 0f
    private var columnsCount = 0

    init {
        if (isInEditMode) {
            setData(
                mutableMapOf(
                    "1" to listOf(
                        Expense(
                            id = 1,
                            name = "Truffo",
                            amount = 4541,
                            category = "Кафе и рестораны",
                            time = 1623326031
                        )
                    )
                )
            )
        }
    }

    fun setData(data: Map<String, Category>) {
        categories = data
        categoryToSum = data.mapValues { pair -> pair.value.sumOf { expense -> expense.amount } }

        data.forEach {
            uniqueDays.addAll(it.value.map { expense -> convertUnixTimestampToDate(expense.time) })
        }
        var curDay = uniqueDays.min()
        val maxDay = uniqueDays.max()
        while (!areDatesOnSameDay(maxDay, curDay)) {
            val newDay = getNextDay(curDay)
            uniqueDays.add(newDay)
            curDay = newDay
        }

        expensesByDays = data.mapValues { pair -> expensesToExpenseByDay(pair.value) }
        sumExpense = expensesByDays.maxOf { it.value.max() }
        sumExpense += 100 - sumExpense % 100

        colors = data.mapValues { generateRandomColor() }
    }

    fun setColors(colors: Map<String, Int>) {
        this.colors = colors
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        widthSize = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> convertToDp(200)
            else -> max(convertToDp(200), widthSize)
        }

        heightSize = when (heightMode) {
            MeasureSpec.UNSPECIFIED -> convertToDp(200)
            else -> max(convertToDp(200), heightSize)
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthSize = 0.9f * width
        heightSize = 0.9f * height
        startX = width - 0.95f * width
        startY = height - 0.05f * height
        columnsCount = uniqueDays.size
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val lineHeight = ceil(-textPaint.ascent() + textPaint.descent()).toInt()
        drawHorizontalLines(canvas, lineHeight)

        drawVerticalLines(canvas, lineHeight)

        expensesByDays.forEach { expenses ->
            brokenLinePaint.color = colors[expenses.key] ?: Color.BLACK
            path.reset()
            path.moveTo(startX, startY)
            expenses.value.forEachIndexed { index, amount ->
                path.lineTo(
                    startX + (index + 1) * (widthSize / columnsCount),
                    startY - heightSize * (amount.toFloat() / sumExpense)
                )
            }
            canvas.drawPath(path, brokenLinePaint)
        }
    }

    private fun drawHorizontalLines(canvas: Canvas, lineHeight: Int): Int {
        textPaint.textAlign = Paint.Align.RIGHT
        path.reset()
        path.moveTo(startX, startY)
        path.lineTo(startX + widthSize, startY)
        canvas.save()
        repeat(4) {
            canvas.drawPath(path, gridPaint)
            canvas.translate(0f, -heightSize / 4)
            canvas.drawText(
                (sumExpense * ((it + 1) / 4f)).toString(),
                startX + widthSize - lineHeight,
                startY + lineHeight,
                textPaint
            )
        }
        canvas.drawPath(path, gridPaint)
        canvas.restore()
        return lineHeight
    }

    private fun drawVerticalLines(canvas: Canvas, lineHeight: Int) {
        textPaint.textAlign = Paint.Align.CENTER
        path.reset()
        path.moveTo(startX, startY)
        path.lineTo(startX, startY - heightSize)
        canvas.save()
        uniqueDays.forEach {
            canvas.drawPath(path, gridPaint)
            canvas.translate(widthSize / columnsCount, 0f)
            canvas.drawText(
                "${it.date}.${it.month}", startX, startY + lineHeight, textPaint
            )
        }
        canvas.drawPath(path, gridPaint)
        canvas.restore()
    }

    override fun onSaveInstanceState(): Parcelable {
        return LinearState(super.onSaveInstanceState()).apply {
            categories = this@LinearView.categories
            sumExpense = this@LinearView.sumExpense
            categoryToSum = this@LinearView.categoryToSum
            colors = this@LinearView.colors
            expensesByDays = this@LinearView.expensesByDays
            uniqueDays = this@LinearView.uniqueDays
            columnsCount = this@LinearView.columnsCount
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is LinearState) {
            super.onRestoreInstanceState(state.superState)
            categories = state.categories
            sumExpense = state.sumExpense
            categoryToSum = state.categoryToSum
            colors = state.colors
            expensesByDays = state.expensesByDays
            uniqueDays = state.uniqueDays
            columnsCount = state.columnsCount
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun expensesToExpenseByDay(expenses: List<Expense>): List<Int> {
        val dayToAmount = mutableListOf<Int>()

        uniqueDays.forEach { date ->
            var sum = 0
            expenses.forEach { expense ->
                if (areDatesOnSameDay(date, convertUnixTimestampToDate(expense.time))) {
                    sum += expense.amount
                }
            }
            dayToAmount.add(sum)
        }

        return dayToAmount
    }
}