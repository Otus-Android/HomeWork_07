package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.util.*

class GraphView(context: Context, attr: AttributeSet) : View(context, attr) {

    private val path = Path()

    private val separatorsPaint = Paint().apply {
        strokeWidth = resources.getDimension(R.dimen.graph_separator_width)
        style = Paint.Style.STROKE
        color = Color.LTGRAY
    }

    private val periodNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.graph_axis_name_text_size)
        color = Color.LTGRAY
    }

    private val graphPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.graph_line_stroke_width)
    }

    private val graphPointPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    private var periodWidth = resources.getDimensionPixelSize(R.dimen.graph_period_width)
    private val rowHeight = resources.getDimensionPixelSize(R.dimen.graph_row_height)

    private val rowTextPadding = 10f
    private val rowPadding
        get() = periodNamePaint.measureText(yPeriods.maxOrNull().toString()) + rowTextPadding

    private val periodPadding =
        resources.getDimensionPixelSize(R.dimen.graph_period_padding).toFloat()

    private val pointHeight
        get() = rowHeight.toFloat() / yPeriods[yPeriods.size - 2].toFloat()

    private var xPeriods: List<String> = emptyList()

    private var yPeriods: List<Int> = emptyList()

    private var points: List<Point> = emptyList()

    private var data: Map<Date, Int> = emptyMap()

    private var defaultWidth = resources.getDimensionPixelSize(R.dimen.graph_period_default_width)
    private var defaultMinWidth = resources.getDimensionPixelSize(R.dimen.graph_period_default_min_width)

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).also { state ->
            state.data = data
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            setData(state.data)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setData(data: Map<Date, Int>) {
        this.data = data
        this.initAxisX(data.keys.toList())
        this.initAxisY(data.values.maxOrNull() ?: 0)
        this.initPoints(data)

        requestLayout()
        invalidate()
    }

    private fun initPoints(data: Map<Date, Int>) {
        points = data.map { pair ->
            val x = rowPadding + xPeriods.indexOf(pair.key.format("MMM d")) * periodWidth
            val y = (yPeriods.size * rowHeight) - pair.value * pointHeight

            Point(x, y)
        }
    }

    private fun initAxisX(dates: List<Date>) {
        this.xPeriods = dates.sorted().toMutableList().apply {
            add(0, this.first().addDays(-1))
            add(this.last().addDays(1))
        }.map { it.format("MMM d") }
    }

    private fun initAxisY(maxAmount: Int) {
        this.yPeriods = (0..maxAmount step maxAmount / 3).toList().sortedDescending()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED ->
                if (data.isEmpty()) 0 else {
                    Math.max(defaultWidth * data.size, defaultMinWidth)
                } else -> MeasureSpec.getSize(widthMeasureSpec)
        }

        val contentHeight = rowHeight * (yPeriods.size + 1)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> contentHeight
            MeasureSpec.EXACTLY -> heightSpecSize
            MeasureSpec.AT_MOST -> contentHeight.coerceAtMost(heightSpecSize)
            else -> error("Unreachable")
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        if (data.isEmpty() || canvas == null) return

        with(canvas) {
            drawGrid()
            drawLine()
        }
    }

    private fun Paint.getTextBaselineByCenter(center: Float) = center - (descent() + ascent()) / 2

    private fun Canvas.drawGrid() {
        yPeriods.forEachIndexed { index, rowName ->
            val startY = rowHeight.toFloat() * index + periodPadding
            val stopX = periodWidth.toFloat() * (xPeriods.size - 1) + rowPadding
            drawLine(rowPadding, startY, stopX, startY, separatorsPaint)

            val name = rowName.toString()
            val nameX = rowPadding - rowTextPadding - periodNamePaint.measureText(name)
            drawText(name, nameX, periodNamePaint.getTextBaselineByCenter(startY), periodNamePaint)
        }

        xPeriods.forEachIndexed { index, periodName ->
            val startX = periodWidth.toFloat() * index + rowPadding
            val stopY = yPeriods.size * rowHeight.toFloat()
            drawLine(startX, periodPadding, startX, stopY, separatorsPaint)

            val nameX = startX - periodNamePaint.measureText(periodName) / 2
            val nameY =
                rowHeight.toFloat() * yPeriods.size - (periodNamePaint.descent() + periodNamePaint.ascent()) + 14f
            drawText(periodName, nameX, nameY, periodNamePaint)
        }
    }

    private fun Canvas.drawLine() {
        path.reset()
        path.moveTo(points[0].x, points[0].y)
        points.forEach {
            path.lineTo(it.x, it.y)
            drawPath(path, graphPaint)
            drawCircle(it.x, it.y, 5f, graphPointPaint)
        }
    }

    private inner class Point(val x: Float, val y: Float)

    private class SavedState : BaseSavedState {

        var data: Map<Date, Int> = emptyMap()

        constructor(parcelable: Parcelable?) : super(parcelable)

        private constructor(parcel: Parcel?) : super(parcel) {
            parcel?.let { parcel.readMap(data, Map::class.java.classLoader) }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeMap(data)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}