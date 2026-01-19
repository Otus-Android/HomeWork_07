package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var data: List<Pair<String, Float>> = emptyList()
    private var colors: List<Int> = listOf(
        ContextCompat.getColor(context,R.color.colorCategory1),
        ContextCompat.getColor(context,R.color.colorCategory2),
        ContextCompat.getColor(context,R.color.colorCategory3),
        ContextCompat.getColor(context,R.color.colorCategory4),
        ContextCompat.getColor(context,R.color.colorCategory5),
        ContextCompat.getColor(context,R.color.colorCategory6),
        ContextCompat.getColor(context,R.color.colorCategory7),
        ContextCompat.getColor(context,R.color.colorCategory8),
        ContextCompat.getColor(context,R.color.colorCategory9),
        ContextCompat.getColor(context,R.color.colorCategory10)
    )
    private var onSectorClickListener: ((String) -> Unit)? = null
    private var angles =
        mutableListOf<Pair<Float, Float>>()


    private fun findClickedSector(x: Float, y: Float): String? {
        val centerX = width / 2f
        val centerY = height / 2f
        val dx = x - centerX
        val dy = y - centerY

        // Проверяем, попадает ли клик в круг
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
        val radius = width / 2f
        if (distance > radius) return null

        // Вычисляем угол клика
        val clickAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val normalizedAngle = (clickAngle + 360) % 360

        // Проверяем, попадает ли угол в сектор
        for (i in angles.indices) {
            val (startAngle, sweepAngle) = angles[i]
            val endAngle = (startAngle + sweepAngle) % 360
            if (normalizedAngle in startAngle..endAngle ||
                (startAngle > endAngle && (normalizedAngle in startAngle..360F || normalizedAngle in 0f..endAngle))) {
                return data[i].first
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val clickedCategory = findClickedSector(event.x, event.y)
            clickedCategory?.let { onSectorClickListener?.invoke(it) }
            return true
        }
        return super.onTouchEvent(event)
    }
    fun setData(newData: List<Pair<String, Float>>) {
        data = newData
        invalidate()
    }

    fun setOnSectorClickListener(listener: (String) -> Unit) {
        onSectorClickListener = listener
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

        val total = data.map { it.second }.sum()
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2f
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = -90f
        angles.clear()

        data.forEachIndexed { index, (category, value) ->
            val sweepAngle = (value / total) * 360f
            paint.color = colors[index % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
            angles.add(startAngle to sweepAngle)
            startAngle += sweepAngle
        }
    }

    private fun handleTouch(x: Float, y: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val dx = x - centerX
        val dy = y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > width / 2f) return

        val angle = (atan2(dy, dx) * (180 / Math.PI) + 360) % 360
        val clickedSectorIndex =
            angles.indexOfFirst { angle >= it.first && angle < it.first + it.second }

        if (clickedSectorIndex != -1) {
            onSectorClickListener?.invoke(data[clickedSectorIndex].first)
        }
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
