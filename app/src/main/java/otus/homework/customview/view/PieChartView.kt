package otus.homework.customview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.PriceData
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5F
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    private val rectF = RectF()
    private val textBounds = Rect()
    private var data: List<PriceData> = listOf()
    private var colors: List<Pair<Int, Int>> = listOf()
    private var gradientColors: List<SweepGradient> = listOf()

    private var callback: Callback? = null

    fun setData(values: List<PriceData>, colors: List<Pair<Int, Int>>) {
        this.data = values
        this.colors = colors
        gradientColors = listOf()
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        if (gradientColors.isEmpty() && colors.isNotEmpty()) {
            colors.forEach {
                gradientColors += SweepGradient(width, height, it.first, it.second)
            }
        }

        val radius = min(width, height) / 2 - 20f
        val holeRadius = radius * 0.77f

        rectF.set(
            width / 2 - radius, height / 2 - radius,
            width / 2 + radius, height / 2 + radius
        )

        var startAngle = 0f
        val dataSum = data.sumOf { it.amount }

        for (i in data.indices) {
            val dataPercent = 100 * data[i].amount.toFloat() / dataSum
            val sweepAngle = (dataPercent / 100) * 360f

            sectionPaint.shader = gradientColors.getOrNull(i % gradientColors.size)

            canvas.drawArc(rectF, startAngle, sweepAngle, true, sectionPaint)
            canvas.drawArc(rectF, startAngle, sweepAngle, true, strokePaint)

            startAngle += sweepAngle
        }

        canvas.drawCircle(width / 2, height / 2, holeRadius, holePaint)

        val centerX = width / 2f
        val centerY = height / 2f

        textPaint.textSize = 60f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        val sumText = "$dataSum ₽"
        val sumBounds = Rect()
        textPaint.getTextBounds(sumText, 0, sumText.length, sumBounds)

        textPaint.textSize = 40f
        textPaint.typeface = Typeface.DEFAULT
        val labelText = "Потрачено в Июне"
        val labelBounds = Rect()
        textPaint.getTextBounds(labelText, 0, labelText.length, labelBounds)

        val totalTextHeight = sumBounds.height() + labelBounds.height() + 16f
        val sumY = centerY - totalTextHeight / 2 + sumBounds.height()
        val labelY = sumY + labelBounds.height() + 16f

        textPaint.textSize = 60f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(sumText, centerX, sumY, textPaint)

        textPaint.textSize = 40f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText(labelText, centerX, labelY, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                val width = width.toFloat()
                val height = height.toFloat()
                val radius = min(width, height) / 2 - 20f
                val centerX = width / 2f
                val centerY = height / 2f

                val distance = sqrt((touchX - centerX).pow(2) + (touchY - centerY).pow(2))
                if (distance <= radius) {
                    val angle = Math.toDegrees(
                        atan2((touchY - centerY).toDouble(), (touchX - centerX).toDouble())
                    ).toFloat()
                    val adjustedAngle = if (angle < 0) angle + 360 else angle

                    var startAngle = 0f
                    val dataSum = data.sumOf { it.amount }

                    for (i in data.indices) {
                        val sweepAngle = (100 * data[i].amount.toFloat() / dataSum) * 360f / 100

                        if (adjustedAngle >= startAngle && adjustedAngle <= startAngle + sweepAngle) {
                            callback?.onSectorClick(data[i])
                            break
                        }
                        startAngle += sweepAngle
                    }
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putFloat("centerX", 0f)
        bundle.putFloat("centerY", 0f)
        bundle.putFloat("radius", 0f)
        bundle.putFloat("innerRadius", 0f)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun applyCallback(callback: Callback) = apply { this.callback = callback }

    interface Callback {
        fun onSectorClick(priceData: PriceData)
    }
} 