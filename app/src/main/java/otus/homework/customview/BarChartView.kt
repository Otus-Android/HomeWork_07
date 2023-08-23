package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BarChartView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val list = ArrayList<PayLoadModel>()
    private var maxValue = 0
    private var barWidth = context.dpToPixels(50)
    private var paintBaseFill : Paint = Paint()
    private var paintText : Paint = Paint()
    private var threshold: Int = Int.MAX_VALUE
    private var paintStroke : Paint = Paint()
    private val rect = RectF()
    private val textAmountSize: Float = context.spToPixels(12)
    private var wSize: Int = 0

    companion object {
        private const val DEFAULT_MARGIN_BAR_X = 30
        private const val DEFAULT_MARGIN_TEXT_BOTTOM_Y = 30
        private const val DEFAULT_MARGIN_TEXT_TOP_Y = 60
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarChartView)
        val baseColor: Int =
            typedArray.getColor(
                R.styleable.BarChartView_baseColor, Color.parseColor("#1A237E"))
        val threshold = typedArray.getInt(R.styleable.BarChartView_threshold, Int.MAX_VALUE)
        val barWidth =
            typedArray.getDimension(
                R.styleable.BarChartView_barWidth, barWidth)
        typedArray.recycle()
        setup(baseColor, threshold, barWidth)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> setMeasuredDimension(wSize, hSize)
            MeasureSpec.AT_MOST -> {
                val newW = Integer.min((list.size * barWidth).toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.UNSPECIFIED -> setMeasuredDimension((list.size * barWidth).toInt(), hSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (list.size == 0) return

        val widthPerView = width.toFloat() / list.size
        var currentX = 0f
        val heightWithMargin = (height - 200)
        val heightPerValue = heightWithMargin.toFloat() / maxValue

        for (item in list) {
            val barX = currentX + DEFAULT_MARGIN_BAR_X
            val barY =
                min((heightWithMargin - heightPerValue * item.amount) + DEFAULT_MARGIN_TEXT_TOP_Y,
                    heightWithMargin.toFloat())
            rect.set(
                barX,
                barY,
                (currentX + widthPerView),
                heightWithMargin.toFloat(),
            )
            canvas.drawRect(rect, paintBaseFill)
            canvas.drawRect(rect, paintStroke)
            canvas.drawText(item.amount.toString(), barX, barY - DEFAULT_MARGIN_TEXT_BOTTOM_Y, paintText)
            currentX += widthPerView
        }

        canvas.drawLine(
            DEFAULT_MARGIN_BAR_X.toFloat(),
            heightWithMargin.toFloat(),
            width.toFloat() + 100,
            heightWithMargin.toFloat() , paintStroke)
    }

    fun setValues(values : List<PayLoadModel>) {
        list.clear()
        list.addAll(values)
        maxValue = list.maxOf { it.amount }

        requestLayout()
        invalidate()
    }

    private fun setup(baseColor: Int, threshold : Int, barWidth : Float) {

        paintBaseFill = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
        }

        paintText = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
            textSize = textAmountSize
        }

        paintStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }
        this.threshold = threshold
        this.barWidth = barWidth
    }
}