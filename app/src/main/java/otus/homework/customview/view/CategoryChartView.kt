package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.PriceData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class CategoryChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#9C27B0".toColorInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#9C27B0".toColorInt()
        alpha = 50
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val axisLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2f
    }

    private var data: List<PriceData> = listOf()
    private var category: String = ""
    private var maxAmount = 0.0
    private var dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
    private var chartPath = Path()
    private var fillPath = Path()

    private var paddingHorizontal = 100f
    private var paddingVertical = 100f
    private var chartWidth = 0f
    private var chartHeight = 0f

    fun setData(newData: List<PriceData>, category: String) {
        this.data = newData.filter { it.category == category }
        this.category = category
        calculateChartDimensions()
        invalidate()
    }

    private fun calculateChartDimensions() {
        if (data.isEmpty()) return

        maxAmount = data.groupBy { it.time * 1000L }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .maxOf { it.value }.toDouble()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chartWidth = w - 2 * paddingHorizontal
        chartHeight = h - 2 * paddingVertical
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) return

        canvas.drawLine(
            paddingHorizontal,
            height - paddingVertical,
            width - paddingHorizontal,
            height - paddingVertical,
            axisLinePaint
        )
        
        canvas.drawLine(
            paddingHorizontal,
            paddingVertical,
            paddingHorizontal,
            height - paddingVertical,
            axisLinePaint
        )

        val dailyExpenses = data.groupBy { it.time * 1000L }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .toSortedMap()

        if (dailyExpenses.isEmpty()) return

        val timeRange = dailyExpenses.lastKey() - dailyExpenses.firstKey()
        val xScale = chartWidth / timeRange
        val yScale = chartHeight / maxAmount

        chartPath.reset()
        fillPath.reset()
        
        var isFirst = true
        dailyExpenses.forEach { (time, amount) ->
            val x = paddingHorizontal + (time - dailyExpenses.firstKey()) * xScale
            val y = height - paddingVertical - (amount * yScale).toFloat()

            if (isFirst) {
                chartPath.moveTo(x, y)
                fillPath.moveTo(x, height - paddingVertical)
                fillPath.lineTo(x, y)
                isFirst = false
            } else {
                chartPath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(
            paddingHorizontal + chartWidth,
            height - paddingVertical
        )
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(chartPath, paint)

        val dateStep = timeRange / 5
        for (i in 0..5) {
            val time = dailyExpenses.firstKey() + dateStep * i
            val x = paddingHorizontal + (time - dailyExpenses.firstKey()) * xScale
            val date = Date(time)
            canvas.drawText(
                dateFormat.format(date),
                x,
                height - paddingVertical + 40,
                textPaint
            )
        }

        val amountStep = maxAmount / 5
        for (i in 0..5) {
            val amount = amountStep * i
            val y = height - paddingVertical - (amount * yScale).toFloat()
            canvas.drawText(
                String.format("%.0f", amount),
                paddingHorizontal - 40,
                y + textPaint.textSize/2,
                textPaint
            )
        }

        textPaint.textSize = 40f
        canvas.drawText(
            category,
            width / 2f,
            paddingVertical - 40,
            textPaint
        )
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putString("category", category)
        bundle.putFloat("chartWidth", chartWidth)
        bundle.putFloat("chartHeight", chartHeight)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            category = state.getString("category", "")
            chartWidth = state.getFloat("chartWidth")
            chartHeight = state.getFloat("chartHeight")
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
} 