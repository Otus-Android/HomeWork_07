package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.min

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var viewSize: Float = 0f
    private var strokeWidth: Float = 0f

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
    }

    private val chartParts = mutableListOf<ChartPart>()

    /** Метод установки значений из json*/
    fun setModels(modelItems: List<ChartItemModel>) {
        chartParts.clear()

        // считаем общую сумму. Для точности переводим в Float
        val totalAmount = modelItems.sumOf { it.amount }.toFloat()
        // считаем какая сумма соответствует одному проценту
        val oneAmountDegree = totalAmount / 360


        // начальное значение с которого будет строиться график
        var startChartDegreePoint = 0f
        modelItems.forEach {
            // считаем сколько градусов занимает значение
            val partDegree = it.amount / oneAmountDegree
            val chartPart = ChartPart(
                startAngle = startChartDegreePoint,
                sweepAngle = partDegree,
                color = generateColor()
            )
            chartParts.add(chartPart)
            startChartDegreePoint += partDegree
        }

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = 1000
        val h = 1000

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        val viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        viewSize = min(viewWidth, viewHeight).toFloat()
        setChartPartWidth(viewSize / 5)

        setMeasuredDimension(viewSize.toInt(), viewSize.toInt())
    }

    private fun setChartPartWidth(width: Float) {
        strokeWidth = width
        paint.strokeWidth = strokeWidth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val center = viewSize / 2

        paint.color = Color.BLACK
        paint.strokeWidth = 5f

        // рисуем линии
        canvas.drawLine(center, 0f, center, viewSize, paint)
        canvas.drawLine(0f, center, viewSize, center, paint)

        paint.strokeWidth = strokeWidth
        chartParts.forEach { it.draw(canvas, paint, viewSize) }
    }

    private fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

}