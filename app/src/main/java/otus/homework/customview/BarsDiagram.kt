package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.random.Random

class BarsDiagram @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): View(context, attrs) {
    private val list = ArrayList<Int>()
    private var maxValue = 0

    private lateinit var paintBase: Paint
    private var barWidth: Int = 20.px
    private lateinit var paintStroke: Paint
    private val rect = RectF()


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarsDiagram)
        val mainColor = typedArray.getColor(R.styleable.BarsDiagram_bar_color, Color.RED)
        setup(mainColor)
        typedArray.recycle()
        if (isInEditMode) {
            val list = mutableListOf<Int>()
            repeat(30){
                list.add(Random.nextInt(20))
            }
            setValues(list)
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
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                val newW = ((list.size) * barWidth)
                setMeasuredDimension(newW, hSize)
            }
        }

        log(
            "____onMeasure____ wMode = ${MeasureSpec.toString(wMode)}  hMode = ${
                MeasureSpec.toString(hMode)
            }, wSize = $wSize, hSize = $hSize   measuredW=${measuredWidth}  measuredH=$measuredHeight"
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (list.size == 0) return

        val widthPerView = width / list.size
        var currentX = 0f
        val heightPerValue = height.toFloat() / (1.5f*maxValue)

        for (item in list) {
            rect.set(
                currentX,
                0f,
                (currentX + widthPerView),
                0f+heightPerValue*item,
            )
            canvas.drawRect(rect, paintBase )
            canvas.drawRect(rect, paintStroke)
            currentX += widthPerView
        }
        canvas.drawLine(0f,0f, width.toFloat(),0f,paintStroke)
    }

    fun setValues(values: List<Int>) {
        list.clear()
        list.addAll(values)
        maxValue = list.max()?: 0
        requestLayout()
        invalidate()
    }

    private fun setup(baseColor: Int) {
        paintBase = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        paintStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5.0f
        }
    }

    private fun log(text: String){
        Log.d(TAG, text)
    }
}