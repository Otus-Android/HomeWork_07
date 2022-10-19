package otus.homework.customview.linerChart

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import otus.homework.customview.BaseChartView
import otus.homework.customview.JsonModel

class LinearChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseChartView(context, attrs) {

    private var chartState = LineChartState()

    private val lineSize = 2.dp()
    private val linePaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = lineSize
    }

    private val textSizeValue = 16.dp()
    private val textPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        color = Color.BLUE
        textAlign = Paint.Align.CENTER
        textSize = textSizeValue
    }

    private val smallTextSizeValue = 12.dp()
    private val smallTextPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        color = Color.RED
        textAlign = Paint.Align.LEFT
        textSize = smallTextSizeValue
    }

    private val chartRadius = 10.dp()

    private val chartPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = lineSize
        pathEffect = CornerPathEffect(chartRadius)
    }

    private val gradientPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        pathEffect = CornerPathEffect(chartRadius)
    }

    private val marginStart = lineSize + textSizeValue / 2
    private val marginBottom = lineSize + textSizeValue + textSizeValue / 2

    private val chartPath = Path()
    private val gradientPath = Path()

    fun drawChartParts(data: Map<String, List<JsonModel>>) {
        chartState.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawChartLines(canvas)
        drawPartLinesOnChart(canvas)
        drawCharValues(canvas)
    }

    private fun drawChartLines(canvas: Canvas) {

        // вертикальная линия ось Y
        canvas.drawLine(
            marginStart,
            0f,
            marginStart,
            height.toFloat() - marginBottom,
            linePaint
        )

        // горизонтальная линия ось X
        canvas.drawLine(
            marginStart,
            height.toFloat() - marginBottom,
            width.toFloat(),
            height.toFloat() - marginBottom,
            linePaint
        )
    }

    /** Ункция которая нарисует черточки на графике*/
    private fun drawPartLinesOnChart(canvas: Canvas) {
        // получить общее количество данных
        val count = chartState.data.entries.sumOf { it.value.size }
        // получили шаг
        val horizontalPart = (width - marginStart) / count

        var prevX = marginStart

        chartState.data.forEach { map ->
            val key = map.key

            val x = if (prevX > marginBottom) {
                prevX + horizontalPart * map.value.size
            } else {
                prevX + horizontalPart * (map.value.size - 1)
            }
            val y = height.toFloat()
            canvas.drawText(key, x, y, textPaint)
            prevX = x

            // рисуем отметку на оси X
            canvas.drawLine(
                x,
                height.toFloat() - marginBottom - textSizeValue / 2,
                x,
                height.toFloat() - marginBottom + textSizeValue / 2,
                linePaint
            )
        }


        val maxHeightValue = (height.toFloat() - marginBottom) * 0.9
        val verticalCount = 5f
        val oneYLine = maxHeightValue.toFloat() / verticalCount
        val maxValue = chartState.data.entries.maxOf { list -> list.value.maxOf { it.amount } }
        val oneYValue = maxValue / verticalCount.toInt()

        for (i in 1..5) {
            canvas.drawLine(
                marginStart - textSizeValue / 2,
                height - marginBottom - (i * oneYLine),
                marginStart + textSizeValue / 2,
                height - marginBottom - (i * oneYLine),
                linePaint
            )

            canvas.drawText(
                (oneYValue * i).toString(),
                marginStart + textSizeValue / 2,
                height - marginBottom - (i * oneYLine) - smallTextSizeValue / 2,
                smallTextPaint
            )
        }

    }

    private fun drawCharValues(canvas: Canvas) {
        val values = chartState.data.values.flatten().sortedBy { it.time }
        val maxValue = values.maxOf { it.amount }

        val maxHeightValue = (height.toFloat() - marginBottom) * 0.9
        val pxInOneAmount = maxHeightValue / maxValue

        // получили шаг
        val horizontalPart = (width - marginStart) / values.size

        gradientPath.reset()
        gradientPath.moveTo(marginStart, height.toFloat() - marginBottom)

        chartPath.reset()
        for (iv in values.withIndex()) {

            val index = iv.index
            val value = iv.value

            val x = marginStart + horizontalPart * index
            val y = (height.toFloat() - marginBottom - pxInOneAmount * value.amount).toFloat()

            if (index == 0) {
                chartPath.moveTo(x, y)
            } else {
                chartPath.lineTo(x, y)
            }
            gradientPath.lineTo(x, y)
        }

        val gradient = LinearGradient(
            0f,
            0f,
            0f,
            maxHeightValue.toFloat(),
            Color.BLUE,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient
        gradientPath.lineTo(
            marginStart + horizontalPart * (values.size - 1),
            (height.toFloat() - marginBottom)
        )
        gradientPath.close()

        canvas.drawPath(gradientPath, gradientPaint)
        canvas.drawPath(chartPath, chartPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return BaseSavedState(chartState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        chartState = (state as BaseSavedState).superState as LineChartState
        super.onRestoreInstanceState(state)
    }

    private fun Int.dp() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )
}