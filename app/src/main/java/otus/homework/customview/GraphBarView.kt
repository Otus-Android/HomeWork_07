package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.data.GraphBarModel

class GraphBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var maxPrice: Int = 2000
    private val stepsPrice: Int = 5
    private val stepsDate: Int = 16
    private val maxVerticalLine = 500f
    private val paddingHorizontal = 50f
    private var list: ArrayList<GraphBarModel> = arrayListOf()

    private val paintText = Paint().apply {
        color = Color.GRAY
    }

    private val paintGrid = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val paintLine = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(wSize, hSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawPrice(canvas)
    }

    fun setMaxPrice(newMaxPrice: Int) {
        maxPrice = newMaxPrice
    }

    private fun drawPrice(canvas: Canvas) {
        val stepPriceForGrid = maxPrice / stepsPrice.minus(1)
        val stepDateForGrid = (measuredWidth - paddingHorizontal.times(2)) / stepsDate.minus(1)

        repeat(stepsPrice) {
            val sum = maxPrice - stepPriceForGrid.times(it)
            canvas.drawLine(
                0f + paddingHorizontal,
                100f + it * 100f,
                measuredWidth.toFloat() - paddingHorizontal,
                100f + it * 100f,
                paintGrid
            )
            canvas.drawText(
                "$sum ₽",
                measuredWidth - 100f,
                50f + it * 100f + paddingHorizontal.div(2),
                paintText.apply { textSize = 32f })
        }

        repeat(stepsDate) {
            canvas.drawLine(
                (it * stepDateForGrid) + paddingHorizontal,
                0f + paddingHorizontal.times(2),
                (it * stepDateForGrid) + paddingHorizontal,
                maxVerticalLine,
                paintGrid
            )
            canvas.drawText(
                if (it == 0) "1" else "${((it + 1) * 2) - 1}",
                (it * stepDateForGrid) + paddingHorizontal - paddingHorizontal.div(4),
                maxVerticalLine.plus(100),
                paintText.apply { textSize = 28f })
        }

        list.map { data ->
            val listDays = data.listCategory.map { it.day }
            paintLine.apply {
                color = data.color
            }

            val path = Path()

            if (data.listCategory.first().day == 1) {
                val sumFirst = data.listCategory.first().sum
                val percentFirst = sumFirst.toFloat().div(maxPrice.toFloat())
                path.moveTo(
                    0f + paddingHorizontal,
                    (maxVerticalLine) - maxVerticalLine.minus(100f).times(percentFirst)
                )

            } else
                path.moveTo(0f + paddingHorizontal, maxVerticalLine)

            repeat(stepsDate.times(2).minus(1)) { repeatInt ->
                if (listDays.contains(repeatInt.plus(1))) {
                    val currSum = data.listCategory.find { it.day == repeatInt.plus(1) }?.sum
                    val percent = currSum?.toFloat()?.div(maxPrice.toFloat()) ?: 0f
                    path.quadTo(
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        (maxVerticalLine) - maxVerticalLine.minus(100f).times(percent),
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        (maxVerticalLine) - maxVerticalLine.minus(100f).times(percent)
                    )
                    path.moveTo(
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        (maxVerticalLine) - maxVerticalLine.minus(100f).times(percent)
                    )
                } else {
                    path.quadTo(
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        maxVerticalLine,
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        maxVerticalLine
                    )
                    path.moveTo(
                        (repeatInt * (stepDateForGrid / 2)) + paddingHorizontal,
                        maxVerticalLine
                    )
                }
            }

            canvas.drawPath(path, paintLine)
        }
    }

    fun setList(newList: List<GraphBarModel>) {
        list.clear()
        list.addAll(newList)
        requestLayout()
        invalidate()
    }
}