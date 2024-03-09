package otus.homework.customview.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.sp

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val chartPadding = 10.dp

    private val linePath = Path()
    private val pointsPath = Path()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.dp
//        pathEffect = CornerPathEffect(10.dp)
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18.sp
    }

    // данные для чарта
    private var data: List<ChartData>? = null

    private var minX = 0L
    private var maxX = 0L
    private var intervalX = 0L

    private var minY = 0
    private var maxY = 0
    private var intervalY = 0

    /**
     * Устанавливает данные для чарта.
     *
     * @param list Список соответствующих объектов данных.
     * @param color Цвет графика.
     */
    fun populate(list: List<ChartData>, color: Int? = null) {
        data = list
        // рассчитать параметры графика
        minX = list.first().time
        maxX = list.last().time
        intervalX = maxX - minX

        minY = list.minBy { it.amount }.amount
        maxY = list.maxBy { it.amount }.amount
        intervalY = maxY - minY

        if (color != null) {
            linePaint.color = color
            textPaint.color = color
            pointPaint.color = color
        }
        requestLayout()
        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (wMode, wSize) = widthMeasureSpec.let {
            MeasureSpec.getMode(it) to MeasureSpec.getSize(it)
        }
        val (hMode, hSize) = heightMeasureSpec.let {
            MeasureSpec.getMode(it) to MeasureSpec.getSize(it)
        }

        // для UNSPECIFIED - минимальный размер
        val w = when (wMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> wSize
            else -> 320.dp.toInt()
        }

        // для UNSPECIFIED - минимальный размер
        val h = when (hMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> hSize
            else -> 240.dp.toInt()
        }

        setMeasuredDimension(w, h)
    }


    override fun onDraw(canvas: Canvas) {
        data.takeIf {
            !it.isNullOrEmpty()
        }?.also { list ->
            canvas.save()

            linePath.reset()
            pointsPath.reset()

            if (list.size == 1) {
                // точка в центре
                val centerX = width / 2f
                val centerY = height / 2f
                drawChartPoint(centerX, centerY)
                canvas.drawPath(pointsPath, pointPaint)
                // текст
                textPaint.textAlign = Paint.Align.CENTER
                canvas.save()
                canvas.rotate(8f, centerX, centerY)
                canvas.drawText(list.first().amount.toString(), centerX, centerY - 16.dp, textPaint)
                canvas.restore()
            } else {
                // линия
                val chartWidth = width - 2 * chartPadding
                val chartHeight = height - 2 * chartPadding

                canvas.translate(chartPadding, chartPadding)

                var x: Float
                var y: Float
                list.forEachIndexed { index, pointData ->
                    if (index == 0) {
                        x = 0f
                        y = 0f
                        linePath.moveTo(x, y)
                    } else {
                        x = (pointData.time - minX) * chartWidth / intervalX
                        y = chartHeight - (pointData.amount - minY) * chartHeight / intervalY
                        linePath.lineTo(x, y)
                    }
                    drawChartPoint(x, y)
                    // текст
                    val xP = x / chartWidth
                    textPaint.textAlign = when {
                        xP < 0.1f -> Paint.Align.LEFT
                        xP > 0.9f -> Paint.Align.RIGHT
                        else -> Paint.Align.CENTER
                    }
                    val textOffsetY = when {
                        y / chartHeight < 0.1f -> 24.dp
                        else -> (-16).dp
                    }
                    canvas.drawText(pointData.amount.toString(), x, y + textOffsetY, textPaint)
                }

//                canvas.drawTextOnPath(list.first().category, linePath, chartPadding, textOffsetY, textPaint)
                canvas.drawPath(linePath, linePaint)
                canvas.drawPath(pointsPath, pointPaint)
            }
            canvas.restore()
        }
    }

    private fun drawChartPoint(x: Float, y: Float) {
        val radius = 6.dp
        pointsPath.moveTo(x - radius, y - radius)
        pointsPath.addCircle(x, y, radius, Path.Direction.CW)
    }

}