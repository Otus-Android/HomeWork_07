package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
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


private const val ONE_RADIAN = 0.0174533f

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
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.parseColor("#00ffc5"),
        Color.parseColor("#ff6800"), Color.parseColor("#bde619"), Color.parseColor("#ddadaf"),
        Color.parseColor("#ff7f50"), Color.parseColor("#7743eb"), Color.parseColor("#872a08"),
        Color.parseColor("#d8bfd8"),
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
        var currentSweepAngle = 0f

        rect.set(left, top, right, bottom)

        paintStroke.textSize = 30f
        paintStroke.textAlign = Paint.Align.LEFT

        // Cycle Draw
        for (item in list) {
            currentSweepAngle = item * oneChunk
            sumAngle += currentSweepAngle
            paintBaseFill.color = colorNew[currentColor]

            //Задаём смещение . dx = 0f без смещения.
            dx = item * oneChunkRect
            dy = dx
            rect.set(left - dx, top - dy, right + dx, bottom + dy)

            //Draw Arc
            canvas.drawArc(rect, currentStartAngle, currentSweepAngle, true, paintBaseFill)

            //Draw Metka . 35f and 10f выравнивание текста.
            val angleForText = (currentStartAngle + (currentSweepAngle / 2f)) * ONE_RADIAN
            val x = widthHalf - 35f + cos(angleForText) * 450f
            val y = heightHalf + 10f + sin(angleForText) * 450f
            canvas.drawText(
                "$item ", x, y, paintStroke
            )
            rect.set(left, top, right, bottom)

            //Next Angle
            currentStartAngle += currentSweepAngle

            //Check last color in ColorNew
            if (currentColor == colorNew.size - 1) {
                currentColor = 0
            } else currentColor += 1
        }

        //Draw White Circle
        canvas.drawCircle(widthHalf, heightHalf, 300f, paintWhite)

        //Draw Coordinate Touch Event
        canvas.drawText("$lastTouchX ", widthHalf - 100f, heightHalf, paintStroke)
        canvas.drawText("$lastTouchY", widthHalf - 100f, heightHalf + 30f, paintStroke)

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

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchSaveInstanceState(container)
    }

    override fun onSaveInstanceState(): Parcelable? {
        Log.i("Normal", "onSaveInstanceState")
        return super.onSaveInstanceState()

    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.i("Normal", "onRestoreInstanceState")
        super.onRestoreInstanceState(state)
    }

}