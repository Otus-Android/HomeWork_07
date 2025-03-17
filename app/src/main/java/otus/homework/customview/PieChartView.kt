package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<DataItem> = emptyList()

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 52f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }

    private var selectedSector: DataItem? = null

    interface OnSectorClickListener {
        fun onSectorClicked(sector: DataItem?)
    }

    private var sectorClickListener: OnSectorClickListener? = null

    fun setOnSectorClickListener(listener: OnSectorClickListener) {
        sectorClickListener = listener
    }

    private var startAngle = 0f

    fun setData(newData: List<DataItem>) {
        this.data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null || data.isEmpty()) return
        val width = width.toFloat()
        val height = height.toFloat()

        val centerX = width / 2
        val centerY = height / 2

        val radius = min(width, height) / 2 * RADIUS_COEFFICIENT

        val totalValue = data.sumOf { it.value.toDouble() }.toFloat()
        var currentAngle = startAngle

        val textItems = mutableListOf<TextItem>()

        for (item in data) {
            paint.color = item.color
            val normalizedAngle = (item.value / totalValue) * 360f
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                currentAngle,
                normalizedAngle,
                true,
                paint
            )

            val middleAngle = currentAngle + normalizedAngle / 2
            val xPosition =
                (centerX + radius * cos(Math.toRadians(middleAngle.toDouble()))).toFloat()
            val yPosition =
                (centerY + radius * sin(Math.toRadians(middleAngle.toDouble()))).toFloat()

            currentAngle += normalizedAngle
            textItems.add(TextItem(item.label, xPosition, yPosition, textPaint))
        }

        for (item in textItems) {
            with(item) {
                if (x > width / 2) {
                    textPaint.textAlign = Paint.Align.RIGHT
                } else {
                    textPaint.textAlign = Paint.Align.LEFT
                }
                canvas.drawText(text, x, y, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    handleTouch(it.x, it.y)
                    return true
                }

                else -> return false
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouch(x: Float, y: Float) {
        val width = width.toFloat()
        val height = height.toFloat()

        val centerX = width / 2
        val centerY = height / 2
        val radius = min(width, height) / 2 * RADIUS_COEFFICIENT

        val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
        if (distance > radius) {
            clearSelectedSector()
            return
        }

        val totalValue = data.sumOf { it.value.toDouble() }.toFloat()
        var currentAngle = startAngle

        for (item in data) {
            val normalizedAngle = (item.value / totalValue) * 360f
            if (currentAngle <= calculateAngle(x, y, centerX, centerY)
                && calculateAngle(x, y, centerX, centerY) < currentAngle + normalizedAngle
            ) {
                selectSector(item)
                break
            }
            currentAngle += normalizedAngle
        }
    }

    private fun calculateAngle(x: Float, y: Float, centerX: Float, centerY: Float): Float {
        val angleInDegrees = atan2(y - centerY, x - centerX) * 180 / PI
        return if (angleInDegrees >= 0) angleInDegrees.toFloat() else (angleInDegrees + 360).toFloat()
    }

    private fun selectSector(sector: DataItem) {
        selectedSector = sector
        sectorClickListener?.onSectorClicked(selectedSector)
        invalidate()
    }

    private fun clearSelectedSector() {
        selectedSector = null
        sectorClickListener?.onSectorClicked(null)
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState).apply {
            dataArray = data.toTypedArray()
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        data = state.dataArray.toList()
        requestLayout()
    }

    internal class SavedState : BaseSavedState {
        lateinit var dataArray: Array<DataItem>

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            dataArray = source.createTypedArray(DataItem.CREATOR) ?: emptyArray()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedArray(dataArray, flags)
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

    private class TextItem(val text: String, val x: Float, val y: Float, val paint: TextPaint)


    data class DataItem(val id: Int, val label: String, val color: Int, val value: Float) :
        Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString().orEmpty(),
            parcel.readInt(),
            parcel.readFloat()
        )

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeInt(id)
            dest?.writeString(label)
            dest?.writeInt(color)
            dest?.writeFloat(value)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<DataItem> {
            override fun createFromParcel(parcel: Parcel): DataItem {
                return DataItem(parcel)
            }

            override fun newArray(size: Int): Array<DataItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val RADIUS_COEFFICIENT = 0.9f
    }
}