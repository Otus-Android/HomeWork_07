package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class GraphChart@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var points: List<Float> = listOf()

    private val axisPaint = Paint()
    private val linePaint = Paint()
    private val path = Path()

    private var xStep = (width / 10).toFloat()
    private var yStep = (height / 20).toFloat()
    private var originY = 0f
    private var originX = 0f

    fun setData(points: List<Float>) {
        this.points = points
    }

    private fun drawAxisLines(canvas: Canvas) {
        canvas.drawLine(xStep, yStep, xStep, height - 10f, axisPaint)
        canvas.drawLine(
            10f, height - yStep,
            width - xStep, height - yStep, axisPaint
        )
    }

    private fun drawChartLines(canvas: Canvas) {
        path.moveTo(originX, originY)
        xStep = width / points.size.toFloat()
        yStep = height / (points.max() - points.min())
        val min = points.min()
        points.onEach {
            path.lineTo(originX, (it - min) * yStep)

            originX += xStep
        }
        canvas.drawPath(path, linePaint)
    }

    private fun drawChart(canvas: Canvas) {
        drawAxisLines(canvas)
        drawChartLines(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = (points.size) * convertToDp(50)
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = (points.size) * convertToDp(50)
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 12f
        linePaint.color = Color.RED

        axisPaint.style = Paint.Style.STROKE
        axisPaint.strokeWidth = 4f
        axisPaint.color = Color.GREEN
        drawChart(canvas)
    }

//    override fun onSaveInstanceState(): Parcelable {
//        return ExpenseState(super.onSaveInstanceState()).apply {
//            this.points = this.points
//        }
//    }
//
//    override fun onRestoreInstanceState(state: Parcelable?) {
//        if (state is ExpenseState) {
//            points = state.points
//        }
//        super.onRestoreInstanceState(state)
//    }

    private fun convertToDp(number: Int) = (context.resources.displayMetrics.density * number).toInt()
}
