package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.utils.px
import java.util.Collections.max
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
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

    private val path = Path()

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
        path.reset()
        var name = 1
        for (item in list) {
            currentSweepAngle = item * oneChunk
            sumAngle += currentSweepAngle
            paintBaseFill.color = colorNew[currentColor]
            dx = item * oneChunkRect
            dy = dx
            rect.set(left - dx, top - dy, right + dx, bottom + dy)


            /*if (currentStartAngle < 90f) {
                rect.offset(dx, dy)
            } else if (currentStartAngle < 180f) {
                rect.offset(-dx, dy)
            } else if (currentStartAngle < 270) {
                rect.offset(-dx, -dy)
            } else {
                rect.offset(dx, -dy)
            }*/

            path.moveTo(widthHalf, heightHalf)

            val newCoordinate = calcPolarCoord(currentStartAngle,450f,widthHalf,heightHalf)

            val x = newCoordinate.first
            val y = newCoordinate.second
            path.lineTo(x, y)
            canvas.drawText(
                "$name $item", x, y, paintStroke
            )
            path.moveTo(widthHalf, heightHalf)

            canvas.drawArc(rect, currentStartAngle, currentSweepAngle, true, paintBaseFill)

            rect.set(left, top, right, bottom)
            paintStroke.textSize = 30f
            paintStroke.textAlign = Paint.Align.LEFT
            canvas.drawText(
                "$item ${currentStartAngle.toInt()}%  [${x}] [${y}] ", left - 100, topText, paintStroke
            )
            topText += 30f
            currentStartAngle += currentSweepAngle

            if (currentColor == 3) {
                currentColor = 0
            }
            currentColor += 1
            name +=1
        }
        path.close()
        canvas.drawPath(path, paintStroke)
        canvas.drawCircle(widthHalf, heightHalf, 300f, paintWhite)

        canvas.drawText("$lastTouchX ",widthHalf - 100f,heightHalf, paintStroke)
        canvas.drawText("$lastTouchY",widthHalf - 100f,heightHalf + 30f, paintStroke)

    }


    private var lastTouchX = 0f
    private var lastTouchY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.x
            lastTouchY = event.y
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val dx = event.x - lastTouchX
            val dy = event.y - lastTouchY

          /*  imgPosX += dx
            imgPosY += dy*/

            lastTouchX = event.x
            lastTouchY = event.y

            invalidate()
        }
        return true
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

    private fun calcPolarCoord(angle : Float, radius: Float , x0: Float,y0: Float) : Pair<Float,Float> {
        val x1 = x0 + cos(angle) * radius
        val y1 = y0 + sin(angle) * radius
        return Pair(x1,y1)
    }

}