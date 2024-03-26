package otus.homework.customview.custom.lineChart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.custom.ChartData
import otus.homework.customview.custom.getRandomColor
import otus.homework.customview.custom.pieChart.Category
import java.text.SimpleDateFormat
import java.util.Date

class LineChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val KEY_COLOR_LIST = "color_list"
    private val KEY_MAP_DATA = "data"

    private var height = 0f
    private var width = 0f

    private var data = listOf<ChartData>()
    private var mapChartData: MutableList<Map<String, Int>> =
        mutableListOf() // key - timestamp, value - total amount
    private var timestampList = listOf<String>()
    private var minAmount = 0
    private var maxAmount = 0
    private val offsetEnd = 12f
    private var offsetStart = 0f
    private var offsetBottom = 0f
    private var widthTime = 0f
    private var heightValue = 0f
    private var eachTimeN = 1

    private val colors = mutableListOf<Int>()

    init {
        for (i in 0..15) {
            colors.add(getRandomColor())
        }
    }

    private val paint = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(30f)
    }

    private val paintBackgroundBorder = Paint().apply {
        color = Color.BLACK
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val paintBackgroundLine = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
    }

    private val path = Path()

    fun setData(data: List<ChartData>) {
        if (data.isEmpty()) return
        this.data = data
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
        offsetStart = rectHorizontal.width().toFloat() + 8
        heightValue = rectHorizontal.height().toFloat() + 8

        val rectVertical = measureTextSize(timestampList[0], textPaint.textSize)
        offsetBottom = rectVertical.height().toFloat() + 8
        widthTime = rectVertical.width().toFloat() + 8

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        height = h - offsetBottom
        width = w - offsetStart - offsetEnd
    }

    override fun onDraw(canvas: Canvas) {
        val heightStep = height / (maxAmount - minAmount)
        val widthStep = if (timestampList.size > 1) width / (timestampList.size - 1) else width

        if (widthTime >= widthStep) {
            eachTimeN = (widthTime / widthStep).toInt() + 1
        }

        canvas.drawLine(offsetStart, 0f, offsetStart, height, paintBackgroundBorder)
        canvas.drawLine(
            offsetStart,
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
                if (indexMap == 0) {
                    path.moveTo(x, height)
                }
                val y = height - ((map.value - minAmount) * heightStep)
                path.lineTo(x, y)

                canvas.drawLine(offsetStart, y, measuredWidth.toFloat(), y, paintBackgroundLine)
                canvas.drawLine(x, 0f, x, height, paintBackgroundLine)

                canvas.drawText(map.value.toString(), 0f, y + offsetBottom, textPaint)
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
        val formatDate = SimpleDateFormat("dd.MM-HH:00")
        val currentDate = Date(timestamp * 1000)
        return formatDate.format(currentDate)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putIntegerArrayList(KEY_COLOR_LIST, ArrayList(colors))
            putParcelableArray(KEY_MAP_DATA, data.toTypedArray())
            putParcelable("superState", superState)
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var savedState = state
        if (savedState is Bundle) {
            val parcelableArray = savedState.getParcelableArray(KEY_MAP_DATA)
            data = parcelableArray?.map { it as ChartData } ?: data
            colors.clear()
            savedState.getIntegerArrayList(KEY_COLOR_LIST)?.toMutableList()?.let { colors.addAll(it) }
            savedState = savedState.getParcelable("superState")
            setData(data)
        }
        super.onRestoreInstanceState(savedState)
    }

}