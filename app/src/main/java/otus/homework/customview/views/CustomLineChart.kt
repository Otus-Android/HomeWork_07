package otus.homework.customview.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.ExpensesItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CustomLineChart(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private var items = listOf<LineData>()
    private val dayWithMonth = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val defaultSize = (160 * Resources.getSystem().displayMetrics.density).toInt()

    private val path = Path()

    private val padding = (40 * Resources.getSystem().displayMetrics.density)

    private val markersX = mutableListOf<Pair<Float, String>>()
    private val markersY = mutableListOf<Pair<Float, String>>()
    private val pair = Pair(0f, "")

    private var maxX = 0L
    private var minX = 0L
    private var maxY = 0f
    private var minY = 0f

    init {
        isSaveEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == MeasureSpec.UNSPECIFIED)
            width = defaultSize
        if (heightMode == MeasureSpec.UNSPECIFIED)
            height = defaultSize
        setMeasuredDimension(width/2, height/4)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        maxX = nextDaySeconds(items.maxOfOrNull { it.time } ?: 0)
        minX = prevDaySeconds(items.minOfOrNull { it.time } ?: 0)

        val dx = (maxX - minX).let { if (it == 0L) 1 else it }
        maxY = (items.maxOfOrNull { it.amount } ?: 0).toFloat() / pointsLocation
        minY = (items.minOfOrNull { it.amount } ?: 0).toFloat() * pointsLocation

        val dy = (maxY - minY).let { if (it == 0f) 1f else it }
        val chartWidth = (right-left).toFloat() - padding * 2
        val chartHeight = (bottom-top).toFloat() - padding * 2

        items.forEachIndexed { i, it ->
            it.x = padding + chartWidth * (it.time - minX) / dx
            it.y = padding + chartHeight - chartHeight * (it.amount - minY) / dy
        }

        val gap = chartWidth / (dx / TIME)
        var currentSeconds = minX
        markersX.clear()

        for (i in 0..(dx / TIME)) {
            markersX.add(pair.copy(i * gap + padding, currentSeconds.toDate()))
            currentSeconds += TIME
        }

        markersY.clear()
        markersY.add(pair.copy(padding + chartHeight, minY.roundToInt().toString()))
        markersY.add(pair.copy(padding, maxY.roundToInt().toString()))
    }

    override fun onDraw(canvas: Canvas?) {
        if (items.isEmpty() || canvas == null) return

        canvas.drawLine(padding, height.toFloat() - padding, padding, 0f, axisPaint)
        canvas.drawLine(
            padding,
            height.toFloat() - padding,
            width.toFloat(),
            height.toFloat() - padding,
            axisPaint
        )

        markersX.forEach {
            if (it.first > padding) {
                canvas.drawLine(
                    it.first,
                    height.toFloat() - padding + getDp(15),
                    it.first,
                    height.toFloat() - padding - getDp(15),
                    axisPaint
                )
            }
            canvas.drawText(it.second, it.first, height.toFloat() - padding + getDp(20), textXPaint)
        }

        markersY.forEachIndexed { i, it ->
            if (i > 0) canvas.drawLine(
                padding - getDp(15), it.first,
                padding + getDp(15), it.first,
                axisPaint
            )
            canvas.drawText(it.second, padding - getDp(5), it.first + getDp(10), textYPaint)
        }

        path.reset()
        path.moveTo(items[0].x, items[0].y)

        items.forEach {
            path.lineTo(it.x, it.y)
            canvas.drawCircle(it.x, it.y, 5f, pointPaint)
        }

        canvas.drawPath(path, chartPaint)
    }

    fun setValues(items: List<ExpensesItem>) {
        this.items = items.map { LineData(it.name, it.amount, it.time) }.sortedBy { it.time }
        requestLayout()
        invalidate()
    }

    private fun prevDaySeconds(time: Long) = Calendar.getInstance().apply {
        timeInMillis = time * 1000
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis / 1000

    private fun nextDaySeconds(time: Long) = Calendar.getInstance().apply {
        timeInMillis = prevDaySeconds(time) * 1000
        add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis / 1000

    private fun Long.toDate() = dayWithMonth.format(this * 1000).toString()

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let {
            return LineState(it).also { it.savedItems = items }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is LineState) {
            super.onRestoreInstanceState(state.superState)
            items = state.savedItems
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    private fun getDp(pixel: Int) : Float {
        return pixel * Resources.getSystem().displayMetrics.density
    }

    companion object {
        private const val TIME = 86400
        private const val pointsLocation = 0.5f

        private val chartPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
        private val pointPaint = Paint().apply {
            color = Color.BLACK
        }
        private val axisPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
        }
        private val textXPaint = Paint().apply {
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = 30f
        }
        private val textYPaint = Paint().apply {
            color = Color.GRAY
            textAlign = Paint.Align.RIGHT
            textSize = 25f
        }
    }
}

class LineState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
    var savedItems = listOf<LineData>()
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(savedItems)
    }
}