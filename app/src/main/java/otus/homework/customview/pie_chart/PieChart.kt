package otus.homework.customview.pie_chart

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.dto.PayloadDto
import java.util.*
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PieChart(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val minSize = resources.getDimensionPixelSize(R.dimen.pie_chart_min_size)
    private var size = minSize

    private val minTextSize =
        resources.getDimensionPixelSize(R.dimen.pie_chart_center_text_min_size)
    private val defaultColor = Color.RED
    private var centerText = ""
    private var centerTextAlpha = 255

    private var pieChartRotation = 0f

    private val gson = Gson()

    private var onChartClickListener: ((List<PayloadDto>) -> Unit)? = null

    private val pieChartSectionsPaint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var sections = listOf<PieChartItem>()

    private var isAnimating = false

    init {
        isSaveEnabled = true
        readAttributes(attrs)

        val json = readPayloadFromRawFiles()
        val itemsList = if (json == null) {
            emptyList()
        } else {
            parsePayloadJson(json)
        }
        val data = itemsList.groupBy { it.category }
        val sumAmount = itemsList.sumOf { it.amount }.toFloat()

        calculatePieChartSections(data, sumAmount)
    }

    private fun readAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChart)
        try {
            textPaint.textSize =
                typedArray.getDimensionPixelSize(R.styleable.PieChart_centerTextSize, minTextSize)
                    .toFloat()
            textPaint.color =
                typedArray.getColor(R.styleable.PieChart_centerTextColor, defaultColor)
            centerText = typedArray.getString(R.styleable.PieChart_android_text) ?: ""
        } finally {
            typedArray.recycle()
        }
    }

    private fun readPayloadFromRawFiles(): String? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.payload)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("PieChart", "Error reading payload from raw files", e)
            null
        }
    }

    private fun parsePayloadJson(json: String): List<PayloadDto> {
        return try {
            gson.fromJson(
                json,
                object : TypeToken<List<PayloadDto>>() {}.type
            )
        } catch (e: Exception) {
            Log.e("PieChart", "Error parsing payload", e)
            emptyList()
        }
    }

    private fun calculatePieChartSections(
        data: Map<String, List<PayloadDto>>,
        sumAmount: Float
    ) {
        val sections = mutableListOf<PieChartItem>()
        var currentAngle = 0F
        data.values.forEach { categoryItems ->
            val sweepAngle = categoryItems.sumOf { it.amount } / sumAmount * 360F
            sections.add(
                PieChartItem(
                    categoryItems,
                    currentAngle,
                    sweepAngle,
                    getColor()
                )
            )
            currentAngle += sweepAngle
        }
        this.sections = sections
    }

    private fun getColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        size = calculateMeasureSize(widthMode, widthSize)
            .coerceAtMost(calculateMeasureSize(heightMode, heightSize))

        pieChartSectionsPaint.strokeWidth = (size / 5).toFloat()
        setMeasuredDimension(size, size)
    }

    private fun calculateMeasureSize(mode: Int, size: Int): Int {
        return when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)
            else -> throw IllegalStateException("Invalid measure mode")
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val strokeWidth = pieChartSectionsPaint.strokeWidth
        val left = strokeWidth / 2
        val top = strokeWidth / 2
        val right = size.toFloat() - strokeWidth / 2
        val bottom = size.toFloat() - strokeWidth / 2
        sections.forEach { section ->
            pieChartSectionsPaint.color = section.color
            canvas?.drawArc(
                left, top,
                right, bottom,
                section.startAngle + pieChartRotation,
                section.sweepAngle,
                false,
                pieChartSectionsPaint
            )
        }

        textPaint.alpha = centerTextAlpha
        canvas?.drawText(
            centerText,
            size.toFloat() / 2,
            size.toFloat() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2),
            textPaint
        )
    }

    fun setOnChartClickListener(callback: ((List<PayloadDto>) -> Unit)?) {
        onChartClickListener = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || isAnimating) {
            return super.onTouchEvent(event)
        }
        val touchX = event.x
        val touchY = event.y

        getTouchPieChartAngle(touchX, touchY)?.let { angle ->
            sections.forEach { section ->
                if (angle > section.startAngle
                    && angle <= section.sweepAngle + section.startAngle
                ) {
                    onChartClickListener?.invoke(section.pieChartItems)
                    animateOnSectionTouch(section)
                    return@forEach
                }
            }
        }

        return super.onTouchEvent(event)
    }

    private fun getTouchPieChartAngle(touchX: Float, touchY: Float): Float? {
        val outerRadius = size / 2
        val innerRadius = outerRadius - pieChartSectionsPaint.strokeWidth
        val centerX = size / 2
        val centerY = size / 2
        Log.e("Touched", "$touchX, $touchY")

        val distanceBetweenCenterAndTouchPoint =
            sqrt((centerX - touchX).pow(2) + (centerY - touchY).pow(2))
        if (distanceBetweenCenterAndTouchPoint < innerRadius || distanceBetweenCenterAndTouchPoint > outerRadius) {
            return null
        }

        val zeroVector = PointF(outerRadius.toFloat(), 0F)
        val touchVector = PointF(touchX - centerX, touchY - centerY)

        val vectorMultiply = zeroVector.x * touchVector.x + zeroVector.y * touchVector.y
        val zeroVectorModule = sqrt(zeroVector.x.pow(2) + zeroVector.y.pow(2))
        val touchVectorModule = sqrt(touchVector.x.pow(2) + touchVector.y.pow(2))

        val angleRadians = acos(vectorMultiply / (zeroVectorModule * touchVectorModule))


        val angleBetweenVectors = Math.toDegrees(angleRadians.toDouble()).toFloat()

        return if (touchY < centerY) {
            360F - angleBetweenVectors
        } else {
            angleBetweenVectors
        }
    }

    private fun animateOnSectionTouch(section: PieChartItem) {
        isAnimating = true
        AnimatorSet().apply {
            playTogether(
                animatePieChartSections(),
                animateCenterText(section)
            )
            doOnEnd { isAnimating = false }
            start()
        }
    }

    private fun animateCenterText(section: PieChartItem): ValueAnimator {
        val animation = ValueAnimator.ofInt(255, 0, 255)
        animation.duration = 2000
        animation.addUpdateListener {
            val animatedValue = it.animatedValue
            if (animatedValue == 0) {
                centerText = "${((section.sweepAngle / 360) * 100).roundToInt()}%"
            }
            centerTextAlpha = it.animatedValue as Int
            invalidate()
        }
        return animation
    }

    private fun animatePieChartSections(): ValueAnimator {
        val animation = ValueAnimator.ofFloat(pieChartRotation, 720f + pieChartRotation)
        animation.duration = 2000
        animation.interpolator = PathInterpolator(0.8f, 0.1f, 0.2f, 0.9f)
        animation.addUpdateListener { valueAnimator ->
            pieChartRotation = (valueAnimator.animatedValue as Float) % 360
            invalidate()
        }
        return animation
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val pieChartState = PieChartSavedState(superState)
        pieChartState.centerText = this.centerText

        return pieChartState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is PieChartSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        this.centerText = state.centerText ?: this.centerText
        invalidate()
    }

    private class PieChartSavedState : BaseSavedState {
        var centerText: String? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            centerText = source.readString()
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeString(centerText)
        }

        companion object CREATOR : Parcelable.Creator<PieChartSavedState> {
            override fun createFromParcel(parcel: Parcel): PieChartSavedState {
                return PieChartSavedState(parcel)
            }

            override fun newArray(size: Int): Array<PieChartSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}