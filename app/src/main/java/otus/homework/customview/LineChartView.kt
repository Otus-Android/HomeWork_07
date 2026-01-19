package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<Pair<String, Float>> = emptyList()

    fun setData(data: List<Pair<String, Float>>) {
        this.data = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 100
        val desiredHeight = 100

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val padding = 150f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Определяем максимальное значение для оси Y
        val maxAmount = data.maxOfOrNull { it.second } ?: 0f
        val yStep = if (maxAmount == 0f) 0f else chartHeight / maxAmount
        var xStep =  chartWidth / data.size
        if (data.size == 1) {
            xStep = chartWidth /2
        }

        // Рисуем ось Y
        canvas.drawLine(padding, padding, padding, height - padding, paintAxis)

        // Рисуем метки на оси Y
        val stepCount = 5
        for (i in 0..stepCount) {
            val value = maxAmount / stepCount * i
            val y = height - padding - (value * yStep)

            // Линии на графике (сетку)
            canvas.drawLine(padding, y, width - padding, y, paintGrid)

            // Подпись значений на оси Y
            canvas.drawText(value.toInt().toString(), padding - 50f, y + 10f, paintText)
        }

        // Рисуем столбцы
        for (i in data.indices) {
            val (date, amount) = data[i]

            val x = padding + i * xStep + xStep / 2
            val y = height - padding - (amount * yStep)
            val barWidth = xStep * 0.8f

            canvas.drawRect(x - barWidth / 2, y, x + barWidth / 2, height - padding, paintBar)

            // Подпись даты под столбцом
            canvas.drawText(date, x - 30f, height - padding + 40f, paintText)
        }
    }

    private val paintAxis = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val paintGrid = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.RIGHT
    }

    private val paintBar = Paint().apply {
        color = ContextCompat.getColor(context,R.color.colorCategory2)
        style = Paint.Style.FILL
    }

     override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, data)
     }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            data = state.data
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        val data: List<Pair<String, Float>>

        constructor(superState: Parcelable?, data: List<Pair<String, Float>>) : super(superState) {
            this.data = data
        }

        private constructor(parcel: Parcel) : super(parcel) {
            data = mutableListOf<Pair<String, Float>>().apply {
                val size = parcel.readInt()
                repeat(size) {
                    add(parcel.readString()!! to parcel.readFloat())
                }
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(data.size)
            data.forEach {
                parcel.writeString(it.first)
                parcel.writeFloat(it.second)
            }
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}