package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.utils.px
import kotlin.math.min

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val list = ArrayList<Int>()
    private var maxValue = 0
    private var barWidth = 80.px.toFloat()
    private lateinit var paintBaseFill: Paint
    private lateinit var paintDangerFill: Paint
    private var threshold: Int = Int.MAX_VALUE
    private lateinit var paintStroke: Paint
    private val rect = RectF()

    init {
        if (isInEditMode) {
            setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000, 809, 1000, 389))
        }

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ChartView)
        val baseColor = typeArray.getColor(R.styleable.ChartView_baseColor,Color.GREEN)
        val dangerColor = typeArray.getColor(R.styleable.ChartView_dangerColor,Color.RED)
        val threshold = typeArray.getInteger(R.styleable.ChartView_threshold,50)
        val barWidth = typeArray.getDimension(R.styleable.ChartView_barWidth, 80.px.toFloat())

        typeArray.recycle()

        setup(baseColor, dangerColor, threshold, barWidth)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                println("ShuView: EXACTLY $wSize $hSize")
                setMeasuredDimension(wSize,hSize)
            }
            MeasureSpec.AT_MOST -> {
                println("ShuView: AT_MOST $wSize $hSize")
                val newW = min((list.size * barWidth).toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.UNSPECIFIED -> {
                println("ShuView: UNSPECIFIED $wSize $hSize")
                setMeasuredDimension((list.size * barWidth).toInt(), wSize)

            }
        }

       // super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (list.size == 0) return

        val widthPerView = width.toFloat() / list.size
        var currentX = 0f
        val heightPerValue = height.toFloat() / maxValue

        //отрисовка списка

        for (item in list) {
            rect.set(
                currentX,
                (height - heightPerValue * item),
                (currentX + widthPerView),
                height.toFloat(),
            )
            canvas.drawRect(rect, if (item > threshold) paintDangerFill else paintBaseFill)
            canvas.drawRect(rect, paintStroke)
            currentX += widthPerView
        }
    }

    fun setValues(values: List<Int>) {
        list.clear()
        list.addAll(values)
        maxValue = list.max()

        requestLayout()
        invalidate()
    }

    fun setThreshold(threshold: Int) {
        this.threshold = threshold

        requestLayout()
        invalidate()
    }

    private fun setup(baseColor: Int, dangerColor: Int, threshold: Int, barWidth: Float) {
        paintBaseFill = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        paintDangerFill = Paint().apply {
            color = dangerColor
            style = Paint.Style.FILL
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