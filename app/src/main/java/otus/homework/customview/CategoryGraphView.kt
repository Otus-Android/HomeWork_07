package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.state.GraphState
import otus.homework.customview.state.PieChartState

class CategoryGraphView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val state = GraphState()

    private val padding = 15f
    private val defSize = 500
    private var actSize = defSize
    private val greyLineStep = 100 //шаг между строками в рублях

    private var category: String? = null
    private val firstDate = 1622494800
    private val lastDate = 1625086800
    private val oneDay = 86400

    val gPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val axisPaint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val linePaint: Paint = Paint().apply {
        color = Color.LTGRAY
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        isAntiAlias = true
    }

    private val orderService = Service()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var size =
            minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        if (size < defSize) size = defSize

        actSize = if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defSize, defSize)
            defSize
        } else {
            setMeasuredDimension(size, size)
            size
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        category?.let {
            drawAxes(canvas, it)

            var lastX = padding
            var lastY = actSize - padding

            if (state.path.isEmpty()) {
                val max = orderService.getOrdersForGraph(it).first
                //итерируем каждую дату в диапазоне
                for (date in firstDate until lastDate step oneDay) {
                    var sum = 0f
                    //итерируем каждый заказ в данной категории
                    for (order in orderService.getOrdersForGraph(it).second) {

                        //если время покупки позже итерируемой даты и раньше этой даты + 1 день
                        //то задаем сумму текущего ордера
                        if (order.time.toLong() > date && order.time.toLong() <= date + oneDay) {
                            sum += order.amount.toFloat()
                        }
                    }
                    val stopY = (actSize - padding) * (1 - (sum / max.toFloat()))

                    canvas.drawLine(lastX, lastY, lastX + 20, stopY, gPaint)
                    lastX += 20
                    lastY = stopY

                    state.addToPath(lastX, lastY)

                    drawTexts(canvas)
                }
            }
        }
    }

    private fun drawAxes(canvas: Canvas, category: String) {
        // ось X
        canvas.drawLine(padding, actSize - padding, actSize - padding, actSize - padding, axisPaint)
        // ось Y
        canvas.drawLine(padding, actSize - padding, padding, padding, axisPaint)

        // строки на графике
        if (state.linesY.isEmpty()) {
            //получаем стоимость самого жирного заказа по данной категории
            val max = orderService.getOrdersForGraph(category).first

            //если его стоимость выше чем шаг линии
            if (max > greyLineStep) {
                for (y in greyLineStep until max step greyLineStep) {
                    //тогда на каждый шаг в 100р от 0 до max  - рисуем линию
                    val lineY = (actSize - padding) * (1 - (y.toFloat() / max.toFloat()))
                    canvas.drawLine(padding, lineY, actSize - padding, lineY, linePaint)
                    state.addToLinesY(lineY)
                }
            }
        } else {
            for (lineY in state.linesY) {
                canvas.drawLine(padding, lineY, actSize - padding, lineY, gPaint)
            }
        }
    }

    fun setCategory(value: String) {
        category = value
        state.clear()
        invalidate()
    }

    private fun drawTexts(canvas: Canvas) {
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Траты", padding, padding, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Дата", actSize - padding, actSize - padding, textPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putSerializable("graphState", state)
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            val restoreState = viewState.getSerializable("graphState") as GraphState
            Log.i("11111", "onRestoreInstanceState: $restoreState")

            viewState = viewState.getParcelable("superState")

        }
        super.onRestoreInstanceState(viewState)
    }
}
