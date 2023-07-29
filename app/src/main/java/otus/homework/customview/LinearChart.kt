package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import otus.homework.utils.px
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import kotlin.math.*


class LinearChart @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
)
    : View(context,attr) {

    private var expensesList = emptyList<Expenses>()
    private var maxSum = 1
    private var startTimeStamp = 0L
    private var finishTimeStamp = 1L
    private var time = 1L
    private var horizontalCoef = 1f
    private var verticalCoef = 1f
    private var pCoef = 0.8f //чтобы графики были не на всю длину оси
    private var padding = 100f
    private var horizontalShift = 0f
    private val defaultViewSize = 250
    private val rectangle = RectF() // прямоугольник графика

    private var expensesByDate = groupAndSumExpenses(expensesList)
    private var path = Path()
    private val paintText = Paint().apply {
        color = ColorUtils.setAlphaComponent(Color.BLACK, 150)
        textSize = 20.px.toFloat()
    }
    private val paintGrid = Paint().apply {
        color = ColorUtils.setAlphaComponent(Color.BLACK, 150)
        pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val paintAxis =  Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val paintList =
        listOf(
            ContextCompat.getColor(context, R.color.pie_0),
            ContextCompat.getColor(context, R.color.pie_1),
            ContextCompat.getColor(context, R.color.pie_2),
            ContextCompat.getColor(context, R.color.pie_3),
            ContextCompat.getColor(context, R.color.pie_4),
            ContextCompat.getColor(context, R.color.pie_5),
            ContextCompat.getColor(context, R.color.pie_6),
            ContextCompat.getColor(context, R.color.pie_7),
            ContextCompat.getColor(context, R.color.pie_8),
            ContextCompat.getColor(context, R.color.pie_9)
        ).map { paintColor ->
            Paint().apply {
                color = paintColor
                style = Paint.Style.STROKE
                strokeWidth = 10f
            }
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawAxes(canvas)
        drawChartLines(canvas)
        drawVerticalGridLines(canvas)
        drawHorizontalGridLines(canvas)
    }

    private fun drawHorizontalGridLines(canvas: Canvas?) {
        val step = maxSum/5
        for (i in 0..4){
            canvas?.drawLine(rectangle.left,
                rectangle.bottom - i*step*verticalCoef,
                rectangle.right,
                rectangle.bottom - i*step*verticalCoef,
                paintGrid)
            canvas?.drawText((i*step).toString(),
                0f,rectangle.bottom - i*step*verticalCoef,paintText)
        }
    }

    private fun drawVerticalGridLines(canvas: Canvas?) {
        for(i in 0..time){
            canvas?.drawLine(   horizontalShift + i*pCoef*(rectangle.right - rectangle.left)/time,
                rectangle.top,
                horizontalShift + i*pCoef*(rectangle.right - rectangle.left)/time,
                rectangle.bottom,
                paintGrid)
            canvas?.drawText((startTimeStamp + i).toString(),
                horizontalShift + i*pCoef*(rectangle.right - rectangle.left)/time,
                rectangle.bottom + paintText.textSize,paintText)
        }
    }

    private fun drawChartLines(canvas: Canvas?) {
        val categories = mutableListOf<String>()
        for(element in expensesByDate) {
            categories.add(element.category)
        }
        val distinctCategories = categories.distinct()
        for(i in distinctCategories.indices) {
            path.reset()
            path.moveTo(horizontalShift,rectangle.bottom)
            for(e  in expensesByDate) {
                if(e.category == distinctCategories[i])
                    path.lineTo(
                        horizontalShift + (e.time - startTimeStamp )*horizontalCoef,
                        rectangle.bottom - e.sum * verticalCoef
                    )
            }
            canvas?.drawPath(path,paintList[i])
        }
    }

    private fun drawAxes(canvas: Canvas?) {
        canvas?.drawLine(rectangle.left,rectangle.top ,rectangle.left,rectangle.bottom, paintAxis)
        canvas?.drawLine(rectangle.left ,rectangle.bottom ,rectangle.right,rectangle.bottom, paintAxis)
    }

    fun setExpenses (expenses: List<Expenses>) {
        expensesList = expenses
        for(e in expensesList){
            e.time = timeStampToDayOfMonth(e.time)
        }
        startTimeStamp = expensesList.minOf { it.time }
        finishTimeStamp = expensesList.maxOf { it.time }
        time = finishTimeStamp - startTimeStamp
        expensesByDate = groupAndSumExpenses(expensesList)
        maxSum = expensesByDate.maxOf { it.sum }
    }

    private fun groupAndSumExpenses(expensesList: List<Expenses>): List<ExpensesByDate> {
        val resultMap = mutableMapOf<String, ExpensesByDate>()

        for (expense in expensesList) {
            val key = "${expense.category}-${expense.time}"
            val existingExpenses = resultMap[key]

            if (existingExpenses == null) {
                resultMap[key] = ExpensesByDate(expense.category, expense.amount, expense.time)
            } else {
                resultMap[key] = existingExpenses.copy(sum = existingExpenses.sum + expense.amount)
            }
        }
        return resultMap.values.toList().sortedBy { it.time }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = calculateSize(widthMeasureSpec)
        val heightSize = calculateSize(heightMeasureSpec)
        rectangle.top = padding
        rectangle.bottom = heightSize.toFloat() - padding
        rectangle.left = padding
        rectangle.right = widthSize.toFloat() - padding
        //
        horizontalCoef = pCoef*(rectangle.right - rectangle.left)/time
        verticalCoef = (rectangle.bottom - rectangle.top)/maxSum
        padding = maxOf(padding, paintText.measureText(maxSum.toString()) + 20)
        horizontalShift = padding + (1-pCoef)*(rectangle.right - rectangle.left)/2
        //
        setMeasuredDimension(widthSize,heightSize)
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> defaultViewSize // например, наш вью будет в скролле
            MeasureSpec.EXACTLY -> size // например, когда задан android:layout_width =200dp или match_parent
            MeasureSpec.AT_MOST -> max(defaultViewSize, size) // например, когда задан layout_width="wrap_content"
            else -> throw IllegalStateException("Wrong MeasureSpec")
        }
    }

    private fun timeStampToDayOfMonth(timestamp: Long): Long {
        val formatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
        val instant = Instant.ofEpochSecond(timestamp)
        val localDate = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return formatter.format(localDate).toLong()
    }
 }