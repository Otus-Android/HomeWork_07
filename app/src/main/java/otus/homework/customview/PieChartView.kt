package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class PieChartView(
    context: Context,
    private val attrs: AttributeSet
) : View(context, attrs) {

    private var mPieChartState: PieChartState = PieChartState.default()
    private val mStrokeWidth =
        resources.getDimensionPixelSize(R.dimen.pieChartStrokeWidth).toFloat()
    private val mPiePaint = Paint().apply {
        strokeWidth = mStrokeWidth
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }
//    private val mPiePaint = Paint().apply {
//        strokeWidth = mStrokeWidth
//        style = Paint.Style.STROKE
//        flags = Paint.ANTI_ALIAS_FLAG
//    }
    private val mMinSize = resources.getDimensionPixelSize(R.dimen.pieChartMinSize)
    private var mSize: Int? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            .takeIf { it >= mMinSize }
            ?: mMinSize
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            .takeIf { it >= mMinSize }
            ?: mMinSize

        if (widthSize > heightSize) {
            widthSize = heightSize
        } else {
            heightSize = widthSize
        }

        mSize = widthSize.takeIf { it <= heightSize } ?: heightSize

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val size = mSize ?: return
        val left = 0f + mStrokeWidth / 2 + paddingLeft
        val right = width.toFloat() - mStrokeWidth / 2 - paddingRight
        val top = 0f + mStrokeWidth / 2 + paddingTop
        val bottom = size.toFloat() - mStrokeWidth / 2 - paddingBottom
        var startAngle = -90f

        mPieChartState.colorStates.forEach { colorState ->
            val color = colorState.color.toInt()
            mPiePaint.color = color
            val sweepAngle = 360 * mPieChartState.getPercentage(colorState.value)
            canvas?.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, mPiePaint)
            startAngle += sweepAngle
        }

       // canvas.drawText("", )
    }

    fun setValue(pieChartState: PieChartState) {
        if (mPieChartState == pieChartState) {
            return
        }

        mPieChartState = pieChartState
        invalidate()
    }

}