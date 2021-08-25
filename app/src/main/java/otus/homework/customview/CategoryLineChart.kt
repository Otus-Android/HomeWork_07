package otus.homework.customview

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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CategoryLineChart(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private var items = listOf<Data>()
    private val dayWithMonth = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val defaultSize = (200 * Resources.getSystem().displayMetrics.density).toInt()

    private val chartPaint = Paint().apply {
        strokeWidth = 12f
        color = Color.BLUE
        style = Paint.Style.STROKE
    }
    private val pointPaint = Paint().apply {
        strokeWidth = 4f
        color = Color.BLUE
    }
    private val axisPaint = Paint().apply {
        strokeWidth = 5f
        color = Color.GRAY
        style = Paint.Style.STROKE
    }
    private val textXPaint = Paint().apply {
        strokeWidth = 3f
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }
    private val textYPaint = Paint().apply {
        strokeWidth = 2f
        color = Color.GRAY
        textAlign = Paint.Align.RIGHT
        textSize = 30f
    }

    private val path = Path()
    private val workAreaPercent = 0.8f
    private var xMax = 0L
    private var xMin = 0L
    private var yMax = 0f
    private var yMin = 0f
    private val padding = 100f
    private val xNotchList = mutableListOf<Pair<Float, String>>()
    private val yNotchList = mutableListOf<Pair<Float, String>>()
    private val pair = Pair(0f, "")

    init {
        isSaveEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) width = defaultSize
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) height = defaultSize
        setMeasuredDimension(width, height)

        xMax = nextDaySeconds(items.maxOfOrNull { it.time } ?: 0)
        xMin = prevDaySeconds(items.minOfOrNull { it.time } ?: 0)
        val dx = (xMax - xMin).let { if (it == 0L) 1 else it }
        yMax = (items.maxOfOrNull { it.amount } ?: 0).toFloat() / workAreaPercent
        yMin = (items.minOfOrNull { it.amount } ?: 0).toFloat() * workAreaPercent
        val dy = (yMax - yMin).let { if (it == 0f) 1f else it }
        val chartWidth = width.toFloat() - padding * 2
        val chartHeight = height.toFloat() - padding * 2
        items.forEachIndexed { i, it ->
            it.x = padding + chartWidth * (it.time - xMin) / dx
            it.y = padding + chartHeight - chartHeight * (it.amount - yMin) / dy
        }

        val gap = chartWidth / (dx / DAY_SECONDS)
        var currentSeconds = xMin
        xNotchList.clear()
        for (i in 0..(dx / DAY_SECONDS)) {
            xNotchList.add(pair.copy(i * gap + padding, currentSeconds.toDate()))
            currentSeconds += DAY_SECONDS
        }

        yNotchList.clear()
        yNotchList.add(pair.copy(padding + chartHeight, yMin.roundToInt().toString()))
        yNotchList.add(pair.copy(padding, yMax.roundToInt().toString()))
    }

    override fun onDraw(canvas: Canvas?) {
        if (items.isEmpty() || canvas == null) return
        canvas.drawLine(padding, height.toFloat() - padding, padding, 0f, axisPaint)
        canvas.drawLine(padding, height.toFloat() - padding, width.toFloat(), height.toFloat() - padding, axisPaint)
        xNotchList.forEach {
            if (it.first > padding) canvas.drawLine(it.first, height.toFloat() - padding + 15f, it.first, height.toFloat() - padding - 15f, axisPaint)
            canvas.drawText(it.second, it.first, height.toFloat() - padding + 45f, textXPaint)
        }
        yNotchList.forEachIndexed { i, it ->
            if (i > 0) canvas.drawLine(padding - 15f, it.first, padding + 15f, it.first, axisPaint)
            canvas.drawText(it.second, padding - 5f, it.first + 10f, textYPaint)
        }
        path.reset()
        path.moveTo(items[0].x, items[0].y)
        items.forEach {
            path.lineTo(it.x, it.y)
            canvas.drawCircle(it.x, it.y, 15f, pointPaint)
        }
        canvas.drawPath(path, chartPaint)
    }

    fun setValues(items: List<DataItem>) {
        this.items = items.map { Data(it.name, it.amount, it.time) }
            .sortedBy { it.time }
//        this.items = listOf(
//            Data("1", 50, 361080),
//            Data("2", 80, 542087),
//            Data("3", 180, 613077),
//            Data("4", 40, 915067),
//            Data("5", 110, 1115756),
//            Data("6", 140, 1290475),
//            Data("7", 70, 1000000)
//        )
//            .sortedBy { it.time }
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
            return State(it).also { it.savedItems = items }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is State) {
            super.onRestoreInstanceState(state.superState)
            items = state.savedItems
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    data class Data(val name: String, val amount: Int, val time: Long) : Parcelable {
        var x = 0f
        var y = 0f

        constructor(parcel: Parcel) : this(
            parcel.readString().orEmpty(),
            parcel.readInt(),
            parcel.readLong()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeInt(amount)
            parcel.writeLong(time)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Data> {
            override fun createFromParcel(parcel: Parcel) = Data(parcel)

            override fun newArray(size: Int): Array<Data?> = arrayOfNulls(size)
        }
    }

    companion object {
        private const val DAY_SECONDS = 86400

        class State : BaseSavedState {

            var savedItems = listOf<Data>()

            constructor(parcelable: Parcelable) : super(parcelable)

            private constructor(parcel: Parcel) : super(parcel) {
                parcel.readList(savedItems, Data::class.java.classLoader)
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeList(savedItems)
            }

            companion object CREATOR : Parcelable.Creator<State> {
                override fun createFromParcel(parcel: Parcel) = State(parcel)

                override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
            }
        }
    }
}