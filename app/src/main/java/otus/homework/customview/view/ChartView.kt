package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
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
    val colorNew = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA,Color.parseColor("#00ffc5"),
        Color.parseColor("#ff6800"),Color.parseColor("#bde619"),Color.parseColor("#ddadaf"),
        Color.parseColor("#ff7f50"),Color.parseColor("#7743eb"),Color.parseColor("#872a08"),
        Color.parseColor("#d8bfd8"),
    )

    init {
        if (isInEditMode) {
            setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000, 809, 1000, 389))
        }

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ChartView)
        val baseColor = typeArray.getColor(R.styleable.ChartView_baseColor, Color.GREEN)
        val dangerColor = typeArray.getColor(R.styleable.ChartView_dangerColor, Color.RED)
        val threshold = typeArray.getInteger(R.styleable.ChartView_threshold, 50)
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
                setMeasuredDimension(wSize, hSize)
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
        var currentColor = 0
        for (item in list) {
            rect.set(
                currentX,
                (height - heightPerValue * item),
                (currentX + widthPerView),
                height.toFloat(),
            )
            paintBaseFill.color = colorNew[currentColor]
            canvas.drawRect(rect,  paintBaseFill)
            canvas.drawRect(rect, paintStroke)

            currentX += widthPerView
            if (currentColor == colorNew.size - 1) {
                currentColor = 0
            } else currentColor += 1
        }
        canvas.drawText("$lastTouchX ",widthPerView + 100f,heightPerValue + 50f, paintStroke)
        canvas.drawText("$lastTouchY",widthPerView + 100f,heightPerValue + 90f, paintStroke)
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
            textSize = 30f
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }
        this.threshold = threshold
        this.barWidth = barWidth
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

}