package otus.homework.customview.chart.pie

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.PayloadItem
import otus.homework.customview.R
import java.util.*
import kotlin.math.*

class PieChart(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val minSize = 100
    private var size = minSize
    private var pieChartPercentText = ""
    private var sections = emptyList<PieChartItem>()
    private var pieChartSelectedCallback: ((List<PayloadItem>) -> Unit)? = null

    private val sectionsPaint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val percentTextPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 70f
        color = Color.RED
    }

    init {
        isSaveEnabled = true
        val payloads = getPayloads()
        val categories = payloads.groupBy { it.category }
        val sumAmount = payloads.sumOf { it.amount }.toFloat()
        calculatePieChartSections(categories, sumAmount)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = calculateMeasureSize(widthMode, widthSize)
        val height = calculateMeasureSize(heightMode, heightSize)

        size = listOf(width, height).minOf { it }

        sectionsPaint.strokeWidth = (size / 4).toFloat()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val strokeWidth = sectionsPaint.strokeWidth
        val left = strokeWidth / 2
        val top = strokeWidth / 2
        val right = size.toFloat() - strokeWidth / 2
        val bottom = size.toFloat() - strokeWidth / 2
        sections.forEach { section ->
            sectionsPaint.color = section.color
            canvas?.drawArc(
                left, top,
                right, bottom,
                section.startAngle,
                section.valueAngle,
                false,
                sectionsPaint
            )
        }

        canvas?.drawText(
            pieChartPercentText,
            size.toFloat() / 2,
            size.toFloat() / 2 - ((percentTextPaint.descent() + percentTextPaint.ascent()) / 2),
            percentTextPaint
        )
    }

    fun setOnClickListener(callback: ((List<PayloadItem>) -> Unit)?) {
        pieChartSelectedCallback = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            getTouchPieChartAngle(event.x, event.y)?.let { angle ->
                for (section in sections) {
                    if (angle > section.startAngle
                        && angle <= section.valueAngle + section.startAngle
                    ) {
                        pieChartSelectedCallback?.invoke(section.pieChartItems)
                        animatePieCartTextPercent(section)
                        break
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getPayloads(): List<PayloadItem> = try {
        val json = context.resources.openRawResource(R.raw.payload).bufferedReader()
            .use { it.readText() }
        Gson().fromJson(json, object : TypeToken<List<PayloadItem>>() {}.type)
    } catch (e: Exception) {
        throw e
    }

    private fun calculatePieChartSections(
        categories: Map<String, List<PayloadItem>>,
        sumAmount: Float
    ) {
        sections = categories.values.mapIndexed { index, categoryItems ->
            val value = categoryItems.sumOf { it.amount } / sumAmount * 360F
            val startAngle = if (index == 0) 0f else {
                categories.values.toList().take(index).flatten()
                    .sumOf { it.amount } / sumAmount * 360F
            }
            PieChartItem(
                startAngle,
                value,
                categoryItems,
                getColor()
            )
        }
    }

    private fun getColor(): Int {
        val rnd = Random()
        return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun getTouchPieChartAngle(touchedX: Float, touchedY: Float): Float? {
        val outerRadius = size / 2
        val innerRadius = outerRadius - sectionsPaint.strokeWidth
        val centerX = size / 2
        val centerY = size / 2

        val distance =
            sqrt((centerX - touchedX).pow(2) + (centerY - touchedY).pow(2))

        if (distance < innerRadius ||
            distance > outerRadius
        ) return null

        return (Math.toDegrees(
            atan2(
                (centerY - touchedY).toDouble(),
                (centerX - touchedX).toDouble()
            )
        )).toFloat() + 180
    }

    private fun animatePieCartTextPercent(section: PieChartItem) {
        val percent = ((section.valueAngle / 360) * 100).roundToInt()
        ValueAnimator.ofInt(0, percent).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                pieChartPercentText = "${it.animatedValue}%"
                invalidate()
            }
            start()
        }
    }

    private fun calculateMeasureSize(mode: Int, size: Int): Int =
        when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)
            else -> throw IllegalStateException("Invalid measure mode")
        }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val pieChartState = PieChartSaveState(superState)
        pieChartState.selectionPercentText = this.pieChartPercentText

        return pieChartState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is PieChartSaveState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        this.pieChartPercentText = state.selectionPercentText ?: this.pieChartPercentText
        invalidate()
    }
}