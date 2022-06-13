package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.round

class PieChartView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var mPieChartState: PieChartState = PieChartState.default()

    // TODO: Перенести в атрибуты
    private val mStrokeWidth =
        resources.getDimensionPixelSize(R.dimen.pieChartStrokeWidth).toFloat()
    private val mPiePaint = Paint().apply {
        strokeWidth = mStrokeWidth
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val mTextPaint = Paint().apply {
        // TODO: Перенести в атрибуты
        textSize = resources.getDimensionPixelSize(R.dimen.pieChartCenterTextSize).toFloat()
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPieChart(canvas)
    }

    fun setValue(pieChartState: PieChartState) {
        if (mPieChartState == pieChartState) {
            return
        }

        mPieChartState = pieChartState
        invalidate()
    }

    private fun drawPieChart(canvas: Canvas) {
        val size = mSize ?: return
        val left = 0f + mStrokeWidth / 2 + paddingLeft
        val right = width.toFloat() - mStrokeWidth / 2 - paddingRight
        val top = 0f + mStrokeWidth / 2 + paddingTop
        val bottom = size.toFloat() - mStrokeWidth / 2 - paddingBottom
        var startAngle = -90f

        mPieChartState.colorStates.forEach { colorState ->
            val selectedOffset = getSelectedOffset(colorState)
            mPiePaint.strokeWidth = mStrokeWidth + selectedOffset * 2

            val separatorSweepAngle = drawSeparator(
                canvas, left, top, right, bottom, selectedOffset, startAngle
            )

            startAngle += separatorSweepAngle
            startAngle += drawPart(
                canvas, left, top, right, bottom, selectedOffset,
                startAngle, colorState, separatorSweepAngle
            )
        }

        drawCenterText(canvas, left, top, right, bottom)
    }

    private fun getSelectedOffset(colorState: PieChartState.ColorState) = when (colorState.id) {
        mPieChartState.selected?.id ->
            // TODO: Перенести в атрибуты
            resources.getDimensionPixelSize(R.dimen.pieChartSelectedOffset)
        else -> 0
    }

    private fun drawSeparator(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        selectedOffset: Int,
        startAngle: Float
    ): Float {
        val separatorSweepAngle = 0.5f
        mPiePaint.color = Color.WHITE
        canvas.drawArc(
            left - selectedOffset,
            top - selectedOffset,
            right + selectedOffset,
            bottom + selectedOffset,
            startAngle,
            separatorSweepAngle,
            false,
            mPiePaint
        )
        return separatorSweepAngle
    }

    private fun drawPart(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        selectedOffset: Int,
        startAngle: Float,
        colorState: PieChartState.ColorState,
        separatorSweepAngle: Float
    ): Float {
        mPiePaint.color = colorState.color.toInt()
        val sweepAngle = 360 * mPieChartState.getPart(colorState.value) - separatorSweepAngle

        canvas.drawArc(
            left - selectedOffset,
            top - selectedOffset,
            right + selectedOffset,
            bottom + selectedOffset,
            startAngle,
            sweepAngle,
            false,
            mPiePaint
        )
        return sweepAngle
    }

    private fun drawCenterText(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val text = when (mPieChartState.selected) {
            null -> DEFAULT_CENTER_TEXT
            else -> mPieChartState.selected?.value?.let {
                "${round(mPieChartState.getPart(it) * 1000) / 10}%"
            } ?: DEFAULT_CENTER_TEXT
        }

        canvas.drawText(
            text,
            left + (right - left) / 2,
            mStrokeWidth / 2 + top + (bottom - top) / 2,
            mTextPaint
        )
    }

    companion object {
        private const val DEFAULT_CENTER_TEXT = "-"
    }

}