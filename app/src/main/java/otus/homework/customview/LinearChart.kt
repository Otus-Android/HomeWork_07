package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import java.lang.Exception
import java.lang.Integer.max
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.format.SignStyle
import java.time.temporal.ChronoField

class LinearChart @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
) : View(context, attr) {

    private var newCategoryList = emptyList<Category>()
    private var maxSum = 1

    private val defaultViewSize = 240
    private val rectangle = RectF()
    private var padding = 100f
    private var horizontalShift = 0f
    private var horizontalMultiply = 1f
    private var verticalMultiply = 1f
    private var paddingMultiply = 0.9f

    private var startTimeStamp = 0L
    private var finishTimeStamp = 1L
    private var time = 1L

    private var categoryByDateList = groupByCategory(newCategoryList)
    private var path = Path()

    private val paintText = Paint()
        .apply {
            color = ColorUtils.setAlphaComponent(Color.GRAY, 255)
            textSize = 20.px.toFloat()
        }

    private val paintGrid = Paint()
        .apply {
            color = ColorUtils.setAlphaComponent(Color.GRAY, 30)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

    private val paintAxis = Paint()
        .apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

    private val paintList = listOf(
        ContextCompat.getColor(context, R.color.color_0),
        ContextCompat.getColor(context, R.color.color_1),
        ContextCompat.getColor(context, R.color.color_2),
        ContextCompat.getColor(context, R.color.color_3),
        ContextCompat.getColor(context, R.color.color_4),
        ContextCompat.getColor(context, R.color.color_5),
        ContextCompat.getColor(context, R.color.color_6),
        ContextCompat.getColor(context, R.color.color_7),
        ContextCompat.getColor(context, R.color.color_8),
        ContextCompat.getColor(context, R.color.color_9)
    ).map { paintColor ->
        Paint().apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawAxes(canvas)
        drawChartLines(canvas)
        drawVerticalGridLines(canvas)
        drawHorizontalGridLines(canvas)
    }

    private fun drawAxes(canvas: Canvas?) {
        with(rectangle) {
            canvas?.drawLine(left, top, left, bottom, paintAxis)
            canvas?.drawLine(left, bottom, right, bottom, paintAxis)
        }
    }

    private fun drawChartLines(canvas: Canvas?) {
        val categories = mutableListOf<String>()
        for (item in categoryByDateList) {
            categories.add(item.category)
        }
        val distinctCategories = categories.distinct()
        for (i in distinctCategories.indices) {
            path.reset()
            path.moveTo(horizontalShift, rectangle.bottom)
            for (item in categoryByDateList) {
                if (item.category == distinctCategories[i])
                    path.lineTo(
                        horizontalShift + (item.time - startTimeStamp) * horizontalMultiply,
                        rectangle.bottom - item.sum * verticalMultiply
                    )
            }
            canvas?.drawPath(path, paintList[i])
        }
    }

    private fun drawHorizontalGridLines(canvas: Canvas?) {
        val step = maxSum / 4
        for (i in 0..3) {
            if (i > 0) {
                canvas?.drawLine(
                    rectangle.left,
                    rectangle.bottom - i * step * verticalMultiply,
                    rectangle.right,
                    rectangle.bottom - i * step * verticalMultiply,
                    paintGrid
                )
            }
            canvas?.drawText(
                (i * step).toString(),
                0f, rectangle.bottom - i * step * verticalMultiply, paintText
            )
        }
    }

    private fun drawVerticalGridLines(canvas: Canvas?) {
        for (i in 0..time) {
            canvas?.drawLine(
                horizontalShift + i * paddingMultiply * (rectangle.right - rectangle.left) / time,
                rectangle.top,
                horizontalShift + i * paddingMultiply * (rectangle.right - rectangle.left) / time,
                rectangle.bottom,
                paintGrid
            )
            canvas?.drawText(
                (startTimeStamp + i).toString(),
                horizontalShift + i * paddingMultiply * (rectangle.right - rectangle.left) / time,
                rectangle.bottom + paintText.textSize, paintText
            )
        }
    }

    fun setData(categoryList: List<Category>) {
        newCategoryList = categoryList
        newCategoryList.forEach { category ->
            category.time = timeStampToDayOfMonth(category.time)
        }

        categoryByDateList = groupByCategory(newCategoryList)
        maxSum = categoryByDateList.maxOf { it.sum }

        startTimeStamp = newCategoryList.minOf { it.time }
        finishTimeStamp = newCategoryList.maxOf { it.time }
        time = finishTimeStamp - startTimeStamp
    }

    private fun groupByCategory(categoryGroups: List<Category>): List<CategoryGroupedByDate> {
        val resultMap = mutableMapOf<String, CategoryGroupedByDate>()
        for (group in categoryGroups) {
            val key = "${group.category}-${group.time}"
            val newCategory = resultMap[key]

            if (newCategory == null) {
                resultMap[key] = CategoryGroupedByDate(group.category, group.amount, group.time)
            } else {
                resultMap[key] = newCategory.copy(sum = newCategory.sum + group.amount)
            }
        }
        return resultMap.values
            .toList()
            .sortedBy { it.time }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = calculateSize(widthMeasureSpec)
        val heightSize = calculateSize(heightMeasureSpec)
        rectangle.apply {
            top = padding
            bottom = heightSize.toFloat() - padding
            left = padding
            right = widthSize.toFloat() - padding
        }
        horizontalMultiply = paddingMultiply * (rectangle.right - rectangle.left) / time
        verticalMultiply = (rectangle.bottom - rectangle.top) / maxSum
        padding = maxOf(padding, paintText.measureText(maxSum.toString()) + 20)
        horizontalShift = padding + (1 - paddingMultiply) * (rectangle.right - rectangle.left) / 2
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> defaultViewSize
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> max(defaultViewSize, size)
            else -> throw Exception("MeasureSpec is not implemented")
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