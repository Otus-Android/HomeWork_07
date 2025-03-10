package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<CostDto> = listOf()
    private var colors: List<Pair<Int, Int>> = listOf()

    fun setData(values: List<CostDto>, colors: List<Pair<Int, Int>>) {
        this.data = values
        this.colors = colors
        invalidate()
    }


    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 5f
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 100f
        val width = width.toFloat()
        val height = height.toFloat()

        val numGridLines = 5
        for (i in 0..numGridLines) {
            val y = height - padding - (i * (height - 2 * padding) / numGridLines)
            canvas.drawLine(padding, y, width - padding, y, gridPaint)
        }

        val localDates = data.associateWith {
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(it.time),
                ZoneId.systemDefault()
            )
        }

        val groupedByCategories = data.groupBy { it.category }

        for ((_, categoryData) in groupedByCategories) {
            val categoryColor = colors.random().first
            linePaint.color = categoryColor

            val maxY = categoryData.maxOf { it.amount } * 1.2f

            val stepX = (width - 2 * padding) / max(1, categoryData.size - 1)
            val stepY = (height - 2 * padding) / maxY

            canvas.drawLine(
                padding,
                height - padding,
                width - padding,
                height - padding,
                axisPaint
            )
            canvas.drawLine(padding, padding, padding, height - padding, axisPaint)

            var prevX: Float? = null
            var prevY: Float? = null

            for ((index, entry) in categoryData.withIndex()) {
                val x = padding + index * stepX
                val y = height - padding - (entry.amount * stepY)

                if (prevX != null && prevY != null) {
                    canvas.drawLine(prevX, prevY, x, y, linePaint)
                }

                canvas.drawCircle(x, y, 10f, linePaint)

                val dateText = localDates[entry]?.format(dateFormatter) ?: ""
                canvas.drawText(dateText, x, height - padding + 40f, textPaint)

                prevX = x
                prevY = y
            }
        }
    }

}