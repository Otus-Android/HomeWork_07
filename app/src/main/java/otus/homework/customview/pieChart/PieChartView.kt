package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /*
    * 1. Добавить сохранение состояния
    * 2. Добавить цвета (ну чтоб были нормальными)
    * 3. Нужно учесть анимацию убывания на несколько секторов
    * 4. В центре сектора написать сколько в нем процентов
    * */


    // событие касания
    private var motionEvent: MotionEvent? = null

    // сектор, который будет уменьшаться
    private var unSelectedChartPart: ChartPart? = null

    // сектор, который будет увеличиваться
    private var selectedChartPart: ChartPart? = null

    private val chartAnimator = ChartAnimator() { animationResult ->
        selectedChartPart?.animate(
            animAngle = animationResult[ChartAnimator.angleKeyInc] ?: 0f,
            animStroke = animationResult[ChartAnimator.strokeKeyInc] ?: 0f
        )
        unSelectedChartPart?.animate(
            animAngle = animationResult[ChartAnimator.angleKeyDec] ?: 0f,
            animStroke = animationResult[ChartAnimator.strokeKeyDec] ?: 0f
        )

        invalidate()
    }

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
    }

    private val chartParts = mutableListOf<ChartPart>()

    /** Метод установки значений из json*/
    fun drawChartParts(data: List<ChartPart>) {
        chartParts.clear()
        chartParts.addAll(data)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (MotionEvent.ACTION_DOWN == event.action) {
            motionEvent = event
            performClick()
            true
        } else {
            false
        }
    }


    override fun performClick(): Boolean {
        super.performClick()

        // получаем часть по которой кликнули
        chartParts.firstOrNull { it.chartTap(motionEvent) }?.let { clickedPart ->

            // установить часть которую будем уменьшаться
            unSelectedChartPart = selectedChartPart

            selectedChartPart = if (clickedPart.name == unSelectedChartPart?.name) {
                null
            } else {
                clickedPart
            }
            selectedChartPart?.name?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }

            chartAnimator.startAnimation()
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cX = width / 2
        val cY = height / 2
        paint.strokeWidth = 2f

        canvas.drawLine(cX.toFloat(), 0f, cX.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0f, cY.toFloat(), width.toFloat(), cY.toFloat(), paint)


        var startAngle = 0f
        chartParts.forEach {
            it.startAngle = startAngle
            startAngle += it.sweepAngle

            it.setViewSize(width, height)
            // рисуем все части кроме кликнутой
            if (selectedChartPart?.name != it.name) {
                it.draw(canvas, paint)
            }
        }

        // рисуем выбранную часть
        selectedChartPart?.draw(canvas, paint)
    }

}