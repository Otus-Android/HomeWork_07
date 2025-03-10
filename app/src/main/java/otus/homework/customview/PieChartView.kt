package otus.homework.customview

import android.R.attr.radius
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
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
    private var data: List<CostDto> = listOf()
    private var colors: List<Pair<Int, Int>> = listOf()
    private var gradientColors: List<SweepGradient> = listOf()

    private var callback: Callback? = null

    fun setData(values: List<CostDto>, colors: List<Pair<Int, Int>>) {
        this.data = values
        this.colors = colors
        invalidate()  // Перерисовка View
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        if (gradientColors.isEmpty() and colors.isNotEmpty()) {
            colors.forEach {
                gradientColors += SweepGradient(width, height, it.first, it.second)
            }
        }

        val radius = min(width, height) / 2 - 20f
        val holeRadius = radius * 0.5f

        rectF.set(
            width / 2 - radius, height / 2 - radius,
            width / 2 + radius, height / 2 + radius
        )

        var startAngle = 0f
        val dataSum = data.sumOf { it.amount }

        for (i in data.indices) {
            val dataPercent = 100 * data[i].amount.toFloat() / dataSum
            val sweepAngle = (dataPercent / 100) * 360f

            sectionPaint.shader = gradientColors[i % gradientColors.size]

            canvas.drawArc(rectF, startAngle, sweepAngle, true, sectionPaint)
            canvas.drawArc(rectF, startAngle, sweepAngle, true, strokePaint)

            startAngle += sweepAngle
        }


        canvas.drawCircle(width / 2, height / 2, holeRadius, holePaint)


        val centerX = width / 2f
        val centerY = height / 2f
        textPaint.getTextBounds(dataSum.toString(), 0, dataSum.toString().length, textBounds)
        val textHeight = textBounds.height()
        val textY = centerY + textHeight / 2f
        canvas.drawText(dataSum.toString(), centerX, textY, textPaint)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                val centerX = width / 2f
                val centerY = height / 2f

                val distance =
                    sqrt((touchX - centerX) * (touchX - centerX) + (touchY - centerY) * (touchY - centerY))
                if (distance <= radius) {

                    val angle = Math.toDegrees(
                        atan2(
                            (touchY - centerY).toDouble(),
                            (touchX - centerX).toDouble()
                        )
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

    fun applyCallback(callback: Callback) = apply { this.callback = callback }

    interface Callback {
        fun onSectorClick(costDto: CostDto)
    }
}