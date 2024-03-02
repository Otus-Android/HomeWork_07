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
import java.util.Collections.max
import kotlin.math.min
import kotlin.random.Random

class ChartCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val list = ArrayList<Int>()
    private var maxValue = 0
    private var sumValues = 0
    private lateinit var paintBaseFill: Paint
    private lateinit var paintDangerFill: Paint
    private lateinit var paintStroke: Paint
    private lateinit var paintWhite: Paint
    private var strokeWidthNew: Float
    private val rect = RectF()
    private var dx = 6f
    private var dy = 6f
    val colorNew = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA
    )

    init {
        if (isInEditMode) {
            setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000, 809, 1000, 389))
            //setValues(listOf(1500, 499, 129, 4541))
            //setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000))
        }

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ChartCircleView)
        strokeWidthNew =
            typeArray.getDimension(R.styleable.ChartCircleView_strokeWidth, 40.px.toFloat())

        typeArray.recycle()

        setup(strokeWidthNew)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                println("ShuView: EXACTLY $wSize $hSize")
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                println("ShuView: AT_MOST $wSize $hSize")
                //   val newW = min((list.size * barWidth).toInt(), wSize)
                //  setMeasuredDimension(newW, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                println("ShuView: UNSPECIFIED $wSize $hSize")
                //   setMeasuredDimension((list.size * barWidth).toInt(), wSize)

            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val widthHalf = width / 2f
        val heightHalf = height / 2f

        //coordinate Rect
        val left = widthHalf - 400f
        val top = heightHalf - 400f
        val right = widthHalf + 400f
        val bottom = heightHalf + 400f

        //Coordinate top text
        var topText = top - 100f
        // Check summa Angle , must 360
        var sumAngle = 0f
        //выбор цвета
        var currentColor = 0

        if (list.size == 0) return

        //Находим один градус
        val oneChunk = 360f / sumValues
        //Находим для ширины
        val oneChunkRect = 100f / sumValues

        //Начальные значения
        var currentStartAngle = 0f
        var currentSweepAngle = 30f

        rect.set(left, top, right, bottom)
        //отрисовка списка

        for (item in list) {
            currentSweepAngle = item * oneChunk
            sumAngle += currentSweepAngle
            paintBaseFill.color = colorNew[currentColor]
            dx = item * oneChunkRect
            dy = dx
            rect.set(left - dx, top - dy, right + dx, bottom + dy)/*if (currentStartAngle < 90f) {
                rect.offset(dx, dy)
            } else if (currentStartAngle < 180f) {
                rect.offset(-dx, dy)
            } else if (currentStartAngle < 270) {
                rect.offset(-dx, -dy)
            } else {
                rect.offset(dx, -dy)
            }*/

            canvas.drawArc(rect, currentStartAngle, currentSweepAngle, true, paintBaseFill)

            rect.set(left, top, right, bottom)
            paintStroke.textSize = 30f
            paintStroke.textAlign = Paint.Align.LEFT
            canvas.drawText(
                "$item ${currentSweepAngle.toInt()}%", left - 100, topText, paintStroke
            )
            topText += 30f
            currentStartAngle += currentSweepAngle

            if (currentColor == 3) { currentColor = 0 }
            currentColor += 1
        }
        canvas.drawCircle(widthHalf, heightHalf, 300f, paintWhite)


    }

    fun setValues(values: List<Int>) {
        list.clear()
        list.addAll(values)
        maxValue = list.max()
        sumValues = list.sum()
        requestLayout()
        invalidate()
    }

    private fun setup(strokeWidthNew: Float) {
        paintBaseFill = Paint().apply {
            color = Color.GREEN
            strokeWidth = strokeWidthNew
            style = Paint.Style.FILL
        }
        paintDangerFill = Paint().apply {
            color = Color.RED
            strokeWidth = strokeWidthNew
            style = Paint.Style.STROKE
        }
        paintStroke = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }
        paintWhite = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    private fun randomColor(): Int {
        return Color.HSVToColor(floatArrayOf(Random.nextInt(361).toFloat(), 1f, 1f))
    }

}