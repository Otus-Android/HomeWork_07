package otus.homework.customview.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.models.Category
import otus.homework.customview.models.Expense
import otus.homework.customview.utils.generateRandomColor
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val arcPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    var colors = mapOf<String, Int>()
        private set

    private var categories: Map<String, Category> = mapOf()
    private var sumExpense: Int = 0
    private var categoryToSum: Map<String, Int> = mapOf()
    private var hasTapped = false
    private var availableRangeForTapped = 0f..0f
    private var tapedAngle = .0
    private var halfSize = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var arcWidth = 0f
    private var textSizes: MutableMap<Int, Float> = mutableMapOf()

    init {
        if (isInEditMode) {
            setData(
                mutableMapOf(
                    "1" to listOf(
                        Expense(
                            id = 4,
                            name = "Truffo",
                            amount = 4541,
                            category = "Кафе и рестораны",
                            time = 1623326031
                        )
                    )
                )
            )
        }
    }

    fun setData(data: Map<String, Category>) {
        categories = data
        categoryToSum = data.mapValues { (_, value) -> value.sumOf { it.amount } }
        sumExpense = categoryToSum.values.sum()
        colors = data.mapValues { generateRandomColor() }
        calculateSizes()
    }

    private fun calculateSizes() {
        halfSize = min(height, width) * 0.5f
        centerX = width / 2f
        centerY = height / 2f
        arcWidth = halfSize / 6
        halfSize -= arcWidth * 1.5f
        textSizes.clear()
        categories.onEachIndexed { index, category ->
            textSizes[index] = determineTextSize(category.key, halfSize * 1.5f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(widthSize, heightSize)

        widthSize = when (widthMode) {
            MeasureSpec.AT_MOST -> {
                size
            }

            else -> {
                widthSize
            }
        }

        heightSize = when (heightMode) {
            MeasureSpec.AT_MOST -> {
                size
            }

            else -> {
                heightSize
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateSizes()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun determineTextSize(text: String, maxWidth: Float): Float {
        var textSize = 60f
        textPaint.textSize = textSize

        while (textPaint.measureText(text) > maxWidth) {
            textSize--
            textPaint.textSize = textSize
        }

        return textSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentAngle = 0f

        availableRangeForTapped = (halfSize - arcWidth / 2)..halfSize + arcWidth / 2

        arcPaint.strokeWidth = arcWidth

        categories.onEachIndexed { index, category ->
            val sweepAngle = (categoryToSum[category.key]?.toFloat() ?: 0f) / sumExpense * 360F
            arcPaint.color = colors[category.key] ?: Color.BLACK
            if (hasTapped && tapedAngle in currentAngle..(currentAngle + sweepAngle)) {
                canvas.drawArc(
                    (centerX - halfSize) - arcWidth,
                    (centerY - halfSize) - arcWidth,
                    centerX + halfSize + arcWidth,
                    centerY + halfSize + arcWidth,
                    currentAngle,
                    sweepAngle,
                    false,
                    arcPaint
                )
                val categoryName = category.key
                val lineHeight = ceil(-textPaint.ascent() + textPaint.descent()).toInt()
                textPaint.textSize = textSizes[index] ?: (halfSize / 10)
                canvas.drawText(categoryName, centerX, centerY, textPaint)
                canvas.drawText(
                    categoryToSum[categoryName].toString(),
                    centerX,
                    centerY + lineHeight,
                    textPaint
                )
            } else {
                canvas.drawArc(
                    centerX - halfSize,
                    centerY - halfSize,
                    centerX + halfSize,
                    centerY + halfSize,
                    currentAngle,
                    sweepAngle,
                    false,
                    arcPaint
                )
            }
            currentAngle += sweepAngle
        }


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }

        val tapedX = event.x
        val tapedY = event.y
        val x = tapedX - width / 2
        val y = tapedY - height / 2

        if (sqrt(x.toDouble().pow(2.0) + y.toDouble().pow(2.0)) in availableRangeForTapped) {
            hasTapped = true
            invalidate()
        } else {
            hasTapped = false
            return super.onTouchEvent(event)
        }

        tapedAngle = atan2(y.toDouble(), x.toDouble()).run { toDegrees(this) }
        if (tapedAngle < 0) {
            tapedAngle += 360
        }

        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        return PieChartState(super.onSaveInstanceState()).apply {
            categories = this@PieChartView.categories
            sumExpense = this@PieChartView.sumExpense
            categoryToSum = this@PieChartView.categoryToSum
            hasTapped = this@PieChartView.hasTapped
            tapedAngle = this@PieChartView.tapedAngle
            colors = this@PieChartView.colors
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PieChartState) {
            super.onRestoreInstanceState(state.superState)
            categories = state.categories
            sumExpense = state.sumExpense
            categoryToSum = state.categoryToSum
            hasTapped = state.hasTapped
            tapedAngle = state.tapedAngle
            colors = state.colors
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}