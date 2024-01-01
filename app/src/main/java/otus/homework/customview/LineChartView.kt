package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

class LineChartView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var points: List<Float> = listOf()

    private var axisPaint: Paint? = null
    private var linePaint: Paint? = null
    private var isInit = false
    private var path: Path? = null

    private var originY = 0f
    private var originX = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var xUnit = 0f
    private var yUnit = 0f

    private fun init() {

        path = Path()

        linePaint = Paint()
        linePaint!!.style = Paint.Style.STROKE
        linePaint!!.strokeWidth = 12f
        linePaint!!.color = Color.CYAN

        axisPaint = Paint()
        axisPaint!!.style = Paint.Style.STROKE
        axisPaint!!.strokeWidth = 4f
        axisPaint!!.color = Color.BLACK

        mWidth = width
        mHeight = height

        xUnit = (mWidth / 10).toFloat()
        yUnit = (mHeight / 20).toFloat()
        originX = xUnit
        originY = mHeight - yUnit

        isInit = true
    }

    fun setData(points: List<Float>) {
        this.points = points
    }

    private fun drawAxisLines(canvas: Canvas) {
        canvas.drawLine(xUnit, yUnit, xUnit, mHeight - 10f, axisPaint!!)
        canvas.drawLine(
            10f, mHeight - yUnit,
            mWidth - xUnit, mHeight - yUnit, axisPaint!!
        )
    }

    private fun drawChartLines(canvas: Canvas) {
        var originX = xUnit
        val originY = mHeight - yUnit
        path!!.moveTo(originX, originY)
        for (i in points.indices) {
            path!!.lineTo(originX + xUnit, originY - points[i] * yUnit)
            canvas.drawCircle(
                originX + xUnit, originY - points[i] * yUnit, 5f, linePaint!!
            )
            originX += xUnit
        }
        canvas.drawPath(path!!, linePaint!!)
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
                width = (points.size) * 50.dp(context).toInt()
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = (points.size) * 50.dp(context).toInt()
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        init()
        drawChart(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        return ExpenseState(super.onSaveInstanceState()).apply {
            this.points = this@LineChartView.points
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ExpenseState) {
            points = state.points
        }
        super.onRestoreInstanceState(state)
    }
}
