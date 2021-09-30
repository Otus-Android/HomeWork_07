package otus.homework.customview.graphicview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.R
import java.util.Calendar

/**
 *
 *
 * @author Юрий Польщиков on 28.09.2021
 */
class GraphicView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var data: GraphicBoundsModel? = null
    private var maxGraphicAmount: Double = 10000.0
    private var maxGraphicDay: Int = 30

    private val path = Path()

    private val netPaint = Paint().apply {
        color = Color.GRAY
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = resources.getDimension(R.dimen.graphic_view_text_size)
    }
    private val pathPaint = Paint().apply {
        color = resources.getColor(R.color.indigo_400, context.theme)
        strokeWidth = 6f // dp ?
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(50f) // dp?
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val size = when (widthMode) {
            MeasureSpec.EXACTLY -> if (data == null) 0 else widthSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> resources.getDimensionPixelSize(R.dimen.pie_chart_size)
            else -> resources.getDimensionPixelSize(R.dimen.pie_chart_size)
        }
        setMeasuredDimension(size, size / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // рисуем вспомогательные горизонтальные линии для шкалы трат
        val amountYStep = (height - (2 * PADDING)) / HORIZONTAL_LINE_COUNT
        canvas.drawLine(PADDING, PADDING, width - PADDING, PADDING, netPaint)
        for (i in 1..4) {
            val y = PADDING + (i * amountYStep)
            canvas.drawLine(PADDING, y, width - PADDING, y, netPaint)
        }

        // рисуем вспомогательные вертикальные линии для шкалы дней месяца
        val timeStep = (width - (2 * PADDING)) / maxGraphicDay
        canvas.drawLine(PADDING, PADDING, PADDING, height - PADDING, netPaint)
        for (i in 1..maxGraphicDay) {
            if (i % 2 != 0) {
                val x = PADDING + ((i - 1) * timeStep)
                canvas.drawLine(x, PADDING, x, height - PADDING, netPaint)
            }
        }
        // рисуем последнюю линию если количество дней в месяце четное
        if (maxGraphicDay % 2 == 0) {
            val x = PADDING + (maxGraphicDay * timeStep)
            canvas.drawLine(x, PADDING, x, height - PADDING, netPaint)
        }

        // рисуем линию графика трат за месяц
        val amountStep = maxGraphicAmount / HORIZONTAL_LINE_COUNT
        data?.let {
            path.reset()
            path.moveTo(PADDING, height - PADDING)
            for (item in it.spendings) {
                val pointX = item.time?.let { time -> PADDING + (time.get(Calendar.DAY_OF_MONTH) - 1) * timeStep }
                val pointY = (height - PADDING) - (item.amount * amountYStep / amountStep)
                pointX?.let {
                    path.lineTo(pointX, pointY.toFloat())
                }
            }
            val endPointX = PADDING + (maxGraphicDay - 1) * timeStep
            path.lineTo(endPointX,height - PADDING)
            canvas.drawPath(path, pathPaint)
            path.close()
        }

        // todo нарисовать текстом месяц (например Май)

        // рисуем шкалу нечетных дней 1 3 5 .. 27 (не учтено что в феврале может быть 28 дней) или 29 или 31
        var y = PADDING + (amountYStep * 4) + (PADDING / 2 + 10)
        for (i in 1..maxGraphicDay) {
            if (i % 2 != 0) {
                val x = PADDING + ((i - 1) * timeStep) - 10

                canvas.drawText("$i", x, y, textPaint)
            }
        }

        // рисуем шкалу затрат в рублях
        // todo по разрядности числа определять координату x
        val x = (maxGraphicDay - 1) * timeStep - PADDING
        for (i in 1..4) {
            val amount = (maxGraphicAmount - (amountStep * (i - 1))).toInt()
            y = PADDING + (i * amountYStep) - (amountYStep - PADDING / 1.5f)
            canvas.drawText("$amount Р", x, y, textPaint)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
        // не придумал что сохранять, так как все задается и обновляется через метод setData
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    /**
     * Подразумевается что данные за один месяц
     */
    fun setData(data: GraphicBoundsModel) {
        this.data = data

        maxGraphicAmount = data.maxAmount + (data.maxAmount / 5)
        maxGraphicDay = data.minTime.getActualMaximum(Calendar.DAY_OF_MONTH)

        requestLayout()
        invalidate()
    }

    companion object {
        private const val HORIZONTAL_LINE_COUNT = 4
        private const val PADDING = 50f // может правильнее использовать dp
    }
}
