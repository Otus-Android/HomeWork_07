package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Integer.min
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

class DetailsChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs) {

    private var items: List<PayItem> = listOf()

    private var chartData: List<PayItem> = listOf()
    private var yStep = 5000
    private var xStep = 1
    private var xCount = 10

    private val defaultHeight = 100.dp
    private val defaultColumnWidth = 32.dp
    private val defaultColumnHeight = 100.dp

    private val calendar = Calendar.getInstance()

    private val noDataPaint: Paint = Paint().apply {
        textSize = 72.sp.toFloat()
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val lineStrokePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1.dp.toFloat()
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(5f, 10f, 5f, 10f), 25f)
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        textSize = 14.sp.toFloat()
    }

    private val chartPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.dp.toFloat()
        pathEffect = CornerPathEffect(2.dp.toFloat())
    }

    val path = Path()

    init {
        if (isInEditMode) {
            setup(listOf(
                PayItem(350, 1694476800),
                PayItem(589, 1694476800),
                PayItem(369, 1694563200),
                PayItem(1000, 1694736000),
                PayItem(349, 1694822400),
            ))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        if (items.isEmpty()) {
            setMeasuredDimension(wSize, max(defaultHeight, hSize))
            return
        }

        val newW: Int = when (wMode) {
            MeasureSpec.EXACTLY -> {
                wSize
            }
            MeasureSpec.AT_MOST -> {
                min(defaultColumnWidth * xCount, wSize)
            }
            else -> {
                defaultColumnWidth * xCount
            }
        }

        when (hMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(newW, min(defaultColumnHeight * 4, hSize))
            }
            else -> {
                setMeasuredDimension(newW, defaultColumnHeight * 4)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (chartData.isEmpty()) {
            val textHeight = noDataPaint.fontMetrics.descent - noDataPaint.fontMetrics.ascent
            canvas.drawText("No data!", (width / 2).toFloat(), height / 2 + textHeight / 3, noDataPaint)
            return
        }

        val x0 = 5f
        val y0 = 5f
        val xEnd = width - 10f
        val yEnd = height - 10f - 14.dp

        val xStepP = (xEnd - x0) / (xCount - 1)
        val yStepP = (yEnd - x0) / 4

        path.reset()
        for (i in 0..4) {
            val y = yStepP * i + y0
            path.moveTo(x0, y)
            path.lineTo(xEnd, y)
        }

        for (i in 0 until xCount) {
            val x = xStepP * i + x0
            path.moveTo(x, y0)
            path.lineTo(x, yEnd)
        }

        canvas.drawPath(path, lineStrokePaint)

        for (i in 0 until xCount) {

            calendar.timeInMillis = chartData[i * xStep].time * 60 * 60 * 24 * 1000
            val str = "${calendar.get(Calendar.DAY_OF_MONTH)}"
            if (i == 0) {
                textPaint.textAlign = Align.LEFT
                canvas.drawText(str, x0, height - 5f, textPaint)
                continue
            }
            if (i == xCount - 1) {
                textPaint.textAlign = Align.RIGHT
                canvas.drawText(str, xStepP * i + x0, height - 5f, textPaint)
                continue
            }

            textPaint.textAlign = Align.CENTER
            canvas.drawText(str, xStepP * i + x0, height - 5f, textPaint)
        }

        for (i in 4 downTo 1) {
            textPaint.textAlign = Align.RIGHT
            canvas.drawText("${yStep * i} р. ", xEnd, (4 - i) * yStepP + 14.dp, textPaint)
        }


        path.reset()
        var x1 = x0
        var y1 = yEnd - (chartData[0].amount.toFloat() / yStep * 4) * yStepP / yStep
        path.moveTo(x1, y1)

        for (i in chartData.withIndex()) {
            if (i.index == 0) {

                continue
            }

            val x2 = xStepP / xStep * i.index
            val y2 = yEnd - i.value.amount * yStepP / yStep
            val xMid = (x2 - x1) / 2 + x1
            path.cubicTo(xMid, y1, xMid, y2, x2, y2)
            x1 = x2
            y1 = y2
        }
        canvas.drawPath(path, chartPaint)

    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(SavedState.keyOneCategory, Gson().toJson(items))
        val superState = super.onSaveInstanceState()
        return SavedState(superState, bundle)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val type = object : TypeToken<List<PayItem>>() {}.type
            items = Gson().fromJson(state.data, type)
            setup(items)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setup(items: List<PayItem>) {
        this.items = items
        val data = items.sortedBy { it.time }
            .map { it.copy(time = it.time / 60 / 60 / 24) }
            .groupBy { it.time }
            .map { (day, amounts) -> PayItem(amounts.sumBy { it.amount }, day) }

        val maxAmount = data.maxOf { it.amount }
        yStep = ceil(maxAmount / 4 / 100f).toInt() * 100

        val maxDay = data.maxOf { it.time }
        val minDay = data.minOf { it.time }

        val verticalCount: Int = when (val tmpCount = maxDay - minDay + 1) {
            in 0..2 -> tmpCount.toInt() + 4
            in 3..4 -> tmpCount.toInt() + 2
            in 5..10 -> tmpCount.toInt()
            else -> ceil(tmpCount / 10f).toInt() * 10
        }

        if (verticalCount > 10) {
            xCount = 10
            xStep = verticalCount / 10
        } else {
            xCount = verticalCount
            xStep = 1
        }

        val prev = (verticalCount - data.size) / 2
        val startDateDaysScience1970 = data[0].time - prev

        val list: MutableList<PayItem> = mutableListOf()
        repeat(verticalCount) {
            val d = startDateDaysScience1970 + it
            list.add(data.find { it.time == d } ?: PayItem(0, d))
        }
        chartData = list
    }

    data class PayItem(val amount: Int, val time: Long)

    private class SavedState : BaseSavedState {

        var data: String = ""

        constructor(superState: Parcelable?, bundle: Bundle) : super(superState) {
            data = bundle.getString(keyOneCategory, "")
        }

        constructor(parcel: Parcel) : super(parcel) {
            data = parcel.readString() ?: ""
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeString(data)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {

            const val keyOneCategory = "keyOneCategory"

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

}
