package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class LineChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    lateinit var lineChartViewModel: LineChartViewModel

    var listData: MutableList<LineData> = mutableListOf()

    private val dayWithMonth = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val defaultSize = (200 * Resources.getSystem().displayMetrics.density).toInt()

    private val path = Path()

    private val padding = 100f

    private val markersX = mutableListOf<Pair<Float, String>>()
    private val markersY = mutableListOf<Pair<Float, String>>()
    private val pair = Pair(0f, "")

    private var maxX = 0L
    private var minX = 0L
    private var maxY = 0f
    private var minY = 0f

    var paints: MutableList<Paint> = mutableListOf()
    private val colors = listOf(
        Color.MAGENTA,
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.GRAY,
        Color.RED,
        Color.YELLOW,
        Color.LTGRAY,
        Color.BLACK,
        Color.DKGRAY
    )

    companion object {
        private const val DAY_SECONDS = 86400
        private const val pointsLocation = 1f

        private val chartPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
        private val axisPaint = Paint().apply {
            color = Color.BLACK
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

    init {
        colors.forEachIndexed { index, element ->
            paints.add(index, Paint().apply {
                color = element
                style = Paint.Style.STROKE
                strokeWidth = 4F
            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)
            width = defaultSize
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)
            height = defaultSize
        setMeasuredDimension(width, height)

        maxX = nextDaySeconds(listData.maxOfOrNull { it.time } ?: 0)
        minX = prevDaySeconds(listData.minOfOrNull { it.time } ?: 0)

        val dx = (maxX - minX).let { if (it == 0L) 1 else it }
        maxY = (listData.maxOfOrNull { it.amount } ?: 0).toFloat() / pointsLocation
        minY = (listData.minOfOrNull { it.amount } ?: 0).toFloat() * pointsLocation

        val dy = (maxY - minY).let { if (it == 0f) 1f else it }
        val chartWidth = width.toFloat() - padding * 2
        val chartHeight = height.toFloat() - padding * 2

        listData.forEachIndexed { i, it ->
            it.x = padding + chartWidth * (it.time - minX) / dx
            it.y = padding + chartHeight - chartHeight * (it.amount - minY) / dy
        }

        val gap = chartWidth / (dx / DAY_SECONDS)
        var currentSeconds = minX
        markersX.clear()

        for (i in 0..(dx / DAY_SECONDS)) {
            markersX.add(pair.copy(i * gap + padding, currentSeconds.toDate()))
            currentSeconds += DAY_SECONDS
        }

        markersY.clear()
        markersY.add(pair.copy(padding + chartHeight, minY.roundToInt().toString()))
        markersY.add(pair.copy(padding, maxY.roundToInt().toString()))
    }

    override fun onDraw(canvas: Canvas?) {
        if (listData.isEmpty() || canvas == null) return

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
                    height.toFloat() - padding + 15f,
                    it.first,
                    height.toFloat() - padding - 15f,
                    axisPaint
                )
            }
            canvas.drawText(it.second, it.first, height.toFloat() - padding + 45f, textXPaint)
        }

        markersY.forEachIndexed { i, it ->
            if (i > 0) canvas.drawLine(
                padding - 15f, it.first,
                padding + 15f, it.first,
                axisPaint
            )
            canvas.drawText(it.second, padding - 5f, it.first + 10f, textYPaint)
        }

        path.reset()
        path.moveTo(padding, height.toFloat() - padding)

        var category = listData[0].category
        var paintIndex = 0
        var paint: Paint = paints[paintIndex]

        listData.forEach { it ->
                if (!category.equals(it.category)) {
                    paint = paints[++paintIndex]
                    path.reset()
                    path.moveTo(padding, height.toFloat() - padding)
                }
                path.lineTo(it.x, it.y)
                canvas.drawPath(path, paint)
                category = it.category
        }
    }

    fun onInit() {
        lineChartViewModel.onInit()
        lineChartViewModel.payloads.observe(context as AppCompatActivity) { payloads ->
            listData.clear()
            payloads.forEach { category ->
                category.value.forEach {
                    listData.add(LineData(it.amount, it.category, it.time))
                }
            }
            requestLayout()
            invalidate()
        }
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
            return LineState(it).also { it.savedItems = listData }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is LineState) {
            super.onRestoreInstanceState(state.superState)
            listData = state.savedItems
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }
}

class LineState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
    var savedItems = mutableListOf<LineData>()
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(savedItems)
    }
}