package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View

class DetailsGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): View(context, attrs) {
    private val list = ArrayList<Int>()
    private var maxValue = 0

    private lateinit var paintBaseFill: Paint
    private lateinit var paintDangerFill: Paint

    private var barWidth: Float = 0f
    private var threshold: Int = 0

    private lateinit var paintStroke: Paint
    private val rect = RectF()


    init {
//        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleChartView)
//        threshold = typedArray.getInt(R.styleable.SimpleChartView_threshold, 0)
//        barWidth = typedArray.getDimension(R.styleable.SimpleChartView_barWidth, 50.px.toFloat())
//        val baseColor = typedArray.getColor(R.styleable.SimpleChartView_baseColor, Color.GREEN)
//        val dangerColor = typedArray.getColor(R.styleable.SimpleChartView_dangerColor, Color.RED)
        setup(Color.GREEN, Color.RED, threshold, barWidth)

//        typedArray.recycle()

        if (isInEditMode) {
            setValues(listOf(2, 4, 5, 12))
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                val newW = Integer.min(((list.size) * barWidth).toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                val newW = ((list.size) * barWidth).toInt()
                setMeasuredDimension(newW, hSize)
            }
        }

        Log.i(TAG, "w = wrapContent, h = 400dp")
        Log.i(
            TAG,
            "____onMeasure____ wMode = ${MeasureSpec.toString(wMode)}  hMode = ${
                MeasureSpec.toString(hMode)
            }, wSize = $wSize, hSize = $hSize"
        )
        Log.i(TAG, "___________________________")
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (list.size == 0) return

        val widthPerView = width.toFloat() / list.size
        var currentX = 0f
        val heightPerValue = height.toFloat() / maxValue

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
        maxValue = list.max()?: 0

        requestLayout()
        invalidate()
    }


//    fun setThreshold(threshold : Int) {
//        this.threshold = threshold
//
//        requestLayout()
//        invalidate()
//    }

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