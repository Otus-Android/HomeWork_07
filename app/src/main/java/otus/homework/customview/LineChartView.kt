package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date

class LineChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var height = 0f
    private var width = 0f

    private var mapChartData: MutableList<Map<String, Int>> = mutableListOf()
    private var timestampList = listOf<String>()

    private var minAmount = 0
    private var maxAmount = 0
    private val offsetEnd = 50f
    private var offsetStart = 50f
    private var offsetBottom = 0f
    private var offsetTop = 50f
    private var widthTime = 0f
    private var heightValue = 0f
    private var eachTimeN = 1

    private val colors = listOf(
        Color.argb(255, 246, 156, 156),
        Color.argb(255, 246, 198, 156),
        Color.argb(255, 246, 236, 156),
        Color.argb(255, 207, 246, 156),
        Color.argb(255, 156, 246, 161),
        Color.argb(255, 156, 246, 205),
        Color.argb(255, 156, 241, 246),
        Color.argb(255, 156, 185, 246),
        Color.argb(255, 196, 156, 246),
        Color.argb(255, 246, 156, 225),
    )

    private val paint = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(30f)
    }

    private val paintBackgroundBorder = Paint().apply {
        color = Color.GRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val paintBackgroundLine = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
    }

    private val path = Path()

    fun setData(data: List<DataItem>) {
        if (data.isEmpty()) return

        val mapChartCategory = data.groupBy { item -> item.category }
        timestampList = data.groupBy { item -> getDate(item.time) }.keys.toList()
        mapChartData.clear()

        mapChartCategory.values.forEach { dataItem ->
            mapChartData.add(dataItem.groupBy { item -> getDate(item.time) }
                .mapValues { list -> list.value.sumOf { it.amount } })
        }

        maxAmount = data.maxOf { it.amount }
        minAmount = data.minOf { it.amount }

        if (maxAmount == minAmount) minAmount = 0

        val rectHorizontal = measureTextSize(maxAmount.toString(), textPaint.textSize)
        offsetStart = rectHorizontal.width().toFloat()
        heightValue = rectHorizontal.height().toFloat()

        val rectVertical = measureTextSize(timestampList[0], textPaint.textSize)
        offsetBottom = rectVertical.height().toFloat() + 20
        widthTime = rectVertical.width().toFloat()

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        height = h - offsetBottom
        width = w - offsetStart - offsetEnd
    }

    override fun onDraw(canvas: Canvas) {
        val heightStep = (height - offsetTop) / (maxAmount - minAmount)
        val widthStep = if (timestampList.size > 1) width / (timestampList.size - 1) else width

        if (widthTime >= widthStep) {
            eachTimeN = (widthTime / widthStep).toInt() + 1
        }

        canvas.drawLine(
            0f,
            0f,
            0f,
            height,
            paintBackgroundBorder
        )
        canvas.drawLine(
            0f,
            height,
            measuredWidth.toFloat(),
            height,
            paintBackgroundBorder
        )

        mapChartData.forEachIndexed { index, maps ->
            paint.color = colors[index]
            path.reset()

            maps.onEachIndexed { indexMap, map ->
                val x = (timestampList.indexOf(map.key) * widthStep) + offsetStart
                if (indexMap == 0) path.moveTo(x, height)
                val y = height - ((map.value - minAmount) * heightStep)
                path.lineTo(x, y)

                canvas.drawLine(offsetStart, y, measuredWidth.toFloat(), y, paintBackgroundLine)
                canvas.drawLine(x, 0f, x, height, paintBackgroundLine)

                canvas.drawText(map.value.toString(), x + 5, y - 5, textPaint)

                if (timestampList.indexOf(map.key) % eachTimeN == 0) {
                    canvas.drawText(map.key, x - offsetStart, measuredHeight.toFloat(), textPaint)
                }
            }

            canvas.drawPath(path, paint)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> maxOf(minimumWidth, widthSize)
            MeasureSpec.AT_MOST -> maxOf(minimumWidth, widthSize)
            else -> minimumWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> maxOf(minimumHeight, heightSize)
            MeasureSpec.AT_MOST -> maxOf(minimumHeight, heightSize)
            else -> minimumHeight
        }

        setMeasuredDimension(width, height)
    }

    private fun measureTextSize(text: String, textSize: Float): Rect {
        val paint = Paint().apply {
            this.textSize = textSize
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        return bounds
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(timestamp: Long): String {
        val formatDate = SimpleDateFormat("dd.MM HH")
        val currentDate = Date(timestamp * 1000)
        return "${formatDate.format(currentDate)} ч"
    }
}