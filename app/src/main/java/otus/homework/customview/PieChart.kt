package otus.homework.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*

class PieChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var data: PieData? = null

    private val chartHeight = MIN_CHART_HEIGHT
    private val padding = 50f
    private val extraPadding = 20

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.BLACK
    }
    private val linePaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = Color.BLACK
    }
    private val mainTextPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
    }

    private val enlargedPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mainRect = RectF()
    private val enlargedRect = RectF()

    private val indicatorPoints = mutableListOf<Pair<Float, Float>>()

    private var onSliceClickListener: OnSliceClickListener? = null

    private var animator: ValueAnimator? = null
    private var currentSliceIdx = -1
    private var currentSelectedSlice: String? = null

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
            putString(SELECTED_SLICE_KEY, currentSelectedSlice)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (newState is Bundle) {
            newState.getString(SELECTED_SLICE_KEY)?.let {
                selectSlice(it)
            }
            newState = newState.getParcelable(SUPER_STATE_KEY)
        }
        super.onRestoreInstanceState(newState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> maxOf(minimumWidth, widthSize)
            MeasureSpec.AT_MOST -> minOf(minimumWidth, maxOf(minimumWidth, widthSize))
            else -> minimumWidth
        }

        val chartHeight = calculateHeight(width)
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> maxOf(chartHeight, heightSize)
            MeasureSpec.AT_MOST -> minOf(chartHeight, maxOf(chartHeight, heightSize))
            else -> chartHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun getMinimumHeight(): Int {
        return MIN_CHART_HEIGHT
    }

    override fun getMinimumWidth(): Int {
        return MIN_CHART_WIDTH
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setPieBounds()
        setChartSizes()
        indicatorPoints.clear()
        data?.pieSlices?.forEach {
            setIndicatorLocation(it.value)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        data?.pieSlices?.let { slices ->
            slices.toList().forEachIndexed { index, (_, slice) ->
                if (currentSelectedSlice == slice.name) {
                    enlargedPaint.color = slice.paint.color
                    canvas.drawArc(enlargedRect, slice.startAngle, slice.sweepAngle, true, enlargedPaint)
                } else if (currentSliceIdx >= index) {
                    canvas.drawArc(mainRect, slice.startAngle, slice.sweepAngle, true, slice.paint)
                    canvas.drawArc(mainRect, slice.startAngle, slice.sweepAngle, true, borderPaint)
                }
            }

            if (currentSliceIdx >= slices.size - 1) {
                data?.pieSlices?.forEach {
                    drawIndicators(canvas, it.value)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                checkPointInPie(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (checkPointInPie(event.x, event.y)) {
                    val angle = getPointAngle(event.x, event.y)
                    val slice = data?.pieSlices?.values?.find {
                        angle > it.startAngle && angle < it.startAngle + it.sweepAngle
                    }
                    slice?.let {
                        selectSlice(it.name)
                    }
                    true
                } else super.onTouchEvent(event)
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun selectSlice(name: String) {
        onSliceClickListener?.onClick(name)
        currentSelectedSlice = name
        invalidate()
    }

    fun setData(data: PieData) {
        this.data = data
        setPieSliceDimensions()
        startInitAnimation()
    }

    fun setOnSliceClickListener(listener: OnSliceClickListener) {
        onSliceClickListener = listener
    }

    private fun startInitAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, data?.pieSlices?.size ?: 0).apply {
            duration = 500
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                currentSliceIdx = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator?.start()
    }

    private fun drawIndicators(canvas: Canvas, pieItem: PieSlice) {
        if (pieItem.indicatorCircleLocation.x < width / 2) {
            drawIndicatorLine(canvas, pieItem, IndicatorAlignment.LEFT)
            drawIndicatorText(canvas, pieItem, IndicatorAlignment.LEFT)
        } else {
            drawIndicatorLine(canvas, pieItem, IndicatorAlignment.RIGHT)
            drawIndicatorText(canvas, pieItem, IndicatorAlignment.RIGHT)
        }
    }

    private fun drawIndicatorLine(canvas: Canvas?, pieItem: PieSlice, alignment: IndicatorAlignment) {
        val xOffset = if (alignment == IndicatorAlignment.LEFT) (width / 4 + extraPadding) * -1 else width / 4
        canvas?.drawLine(
            pieItem.indicatorCircleLocation.x, pieItem.indicatorCircleLocation.y,
            pieItem.indicatorCircleLocation.x + xOffset, pieItem.indicatorCircleLocation.y, linePaint
        )
    }

    private fun drawIndicatorText(canvas: Canvas?, slice: PieSlice, alignment: IndicatorAlignment) {
        val xOffset = if (alignment == IndicatorAlignment.LEFT) (width / 4 + extraPadding) * -1 else width / 4
        if (alignment == IndicatorAlignment.LEFT) mainTextPaint.textAlign = Paint.Align.LEFT
        else mainTextPaint.textAlign = Paint.Align.RIGHT
        val formatText = if (slice.name.length > 14) slice.name.substring(0, 14) else slice.name
        canvas?.drawText(
            formatText, slice.indicatorCircleLocation.x + xOffset,
            slice.indicatorCircleLocation.y - 8, mainTextPaint
        )
    }

    private fun setPieSliceDimensions() {
        var lastAngle = 0f
        indicatorPoints.clear()

        val totalValue = data?.totalValue ?: 0f
        data?.pieSlices?.forEach {
            it.value.startAngle = lastAngle
            it.value.sweepAngle = if (totalValue != 0f) ((it.value.value / totalValue)) * 360f else 0f
            lastAngle += it.value.sweepAngle

            setIndicatorLocation(it.value)
        }
    }

    private fun setIndicatorLocation(slice: PieSlice) {
        val middleAngle = slice.sweepAngle / 2.0 + slice.startAngle

        var positionCoefficient = 5 / 12f
        var indicatorX: Float
        var indicatorY: Float

        do {
            indicatorX = positionCoefficient * chartHeight *
                    cos(Math.toRadians(middleAngle)).toFloat() + width / 2
            indicatorY = positionCoefficient * chartHeight *
                    sin(Math.toRadians(middleAngle)).toFloat() + chartHeight / 2 + padding

            val sideEqualPoints = indicatorPoints.filter { it.first < width / 2 == indicatorX < width / 2 }
            val countIntersectionPoints =
                sideEqualPoints.filter { abs(it.second - indicatorY) < mainTextPaint.textSize * 1.2 }.size
            positionCoefficient += 1 / 64f
        } while (countIntersectionPoints > 0)

        if (checkPointInPie(indicatorX, indicatorY)) {
            slice.indicatorCircleLocation.apply {
                x = indicatorX
                y = indicatorY
            }
            indicatorPoints.add(indicatorX to indicatorY)
        }
    }

    private fun checkPointInPie(x: Float, y: Float): Boolean {
        val radius = height / 2 - padding
        return (x - width / 2).pow(2) + (y - height / 2).pow(2) < radius.pow(2)
    }

    private fun getPointAngle(x: Float, y: Float): Float {
        var angle = Math.toDegrees(atan2(y - (height / 2 - padding), (x - width / 2)).toDouble()).toFloat()
        if (angle < 0)
            angle += 360f

        return angle
    }

    private fun setPieBounds(
        top: Float = padding, bottom: Float = chartHeight.toFloat() + padding,
        left: Float = (width / 2) - (chartHeight / 2).toFloat(),
        right: Float = (width / 2) + (chartHeight / 2).toFloat()
    ) {
        enlargedRect.top = top - extraPadding
        enlargedRect.bottom = bottom + extraPadding
        enlargedRect.left = left - extraPadding
        enlargedRect.right = right + extraPadding

        mainRect.top = top
        mainRect.bottom = bottom
        mainRect.left = left
        mainRect.right = right
    }

    private fun setChartSizes() {
        mainTextPaint.textSize = chartHeight / 16f
        borderPaint.strokeWidth = chartHeight / 256f
        linePaint.strokeWidth = chartHeight / 128f
    }

    enum class IndicatorAlignment {
        LEFT, RIGHT
    }

    interface OnSliceClickListener {
        fun onClick(category: String)
    }

    companion object {
        private const val SELECTED_SLICE_KEY = "SELECTED_SLICE_KEY"
        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"

        private const val MIN_CHART_WIDTH = 800
        private const val ASPECT_RATIO_COEFFICIENT = 0.7
        private const val MIN_CHART_HEIGHT = (ASPECT_RATIO_COEFFICIENT * MIN_CHART_WIDTH).toInt()

        fun calculateHeight(width: Int = MIN_CHART_WIDTH) = (ASPECT_RATIO_COEFFICIENT * width).toInt()
    }
}