package otus.homework.customview.lineChart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import otus.homework.customview.lineChart.LineChartState.Companion.requireDates
import java.util.*
import kotlin.math.round


class LineChartView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var mState: LineChartState = LineChartState.default()

    private val mLinePaint = Paint().apply {
        // TODO: Перенести в атрибуты
        strokeWidth = 4f
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        pathEffect = CornerPathEffect(8f)
    }
    private val mTextPaint = Paint().apply {
        // TODO: Перенести в атрибуты
        textSize = resources.getDimensionPixelSize(R.dimen.lineChartTextSize).toFloat()
        color = ContextCompat.getColor(context, R.color.axis_info)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val mGradientPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        pathEffect = CornerPathEffect(8f)
    }

    private val mAxisPaint = Paint().apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.axis)
    }

    private val mLinePath = Path()
    private val mClipRect = RectF()

    fun setValue(state: LineChartState) {
        if (state == mState) {
            return
        }

        mState = state
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val offsetX = 150
        val offsetY = 50f
        when (mState) {
            is LineChartState.Dates -> {
                val firstMonth = mState.requireDates().minDate?.get(Calendar.MONTH)
                if (firstMonth != null) {
                    val listValues = (mState as LineChartState.Dates).getDatesByMonth(firstMonth)
                    drawChart(
                        canvas = canvas,
                        values = listValues,
                        color = mState.requireDates().color.toInt(),
                        offset = offsetX,
                        month = firstMonth
                    )
                    drawAxisInfo(canvas, mState.requireDates(), offsetX, firstMonth, offsetY)
                } else {
                    drawEmpty(canvas)
                }
            }
        }
        drawCoordinateAxis(canvas, offsetX, offsetY)
    }

    private fun drawEmpty(canvas: Canvas) {
        mTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "No data",
            (width.toFloat() / 2),
            height.toFloat() / 2,
            mTextPaint
        )
    }

    private fun drawCoordinateAxis(canvas: Canvas, offsetX: Int, offsetY: Float) {
        canvas.drawLine(
            offsetX.toFloat(),
            0f,
            offsetX.toFloat(),
            height.toFloat() - offsetY,
            mAxisPaint
        )
        canvas.drawLine(
            offsetX.toFloat(),
            height - offsetY,
            width.toFloat(),
            height - offsetY,
            mAxisPaint
        )
    }

    private fun drawAxisInfo(
        canvas: Canvas,
        state: LineChartState.Dates,
        offsetX: Int,
        month: Int,
        offsetY: Float
    ) {
        val countY = 4
        val intervalValue = round(state.getMaxValueByMonth(month) * 10f) / 10 / countY
        val intervalY = (height - offsetY) / countY

        mTextPaint.textAlign = Paint.Align.LEFT

        repeat(countY) {
            canvas.drawText(
                (intervalValue * (it + 1)).toString(),
                10f,
                height - intervalY * (it + 1),
                mTextPaint
            )
        }

        val minDate = state.getMinDateByMonth(month)?.get(Calendar.DAY_OF_MONTH) ?: return
        val maxDate = state.getMaxDateByMonth(month)?.get(Calendar.DAY_OF_MONTH) ?: return
        val intervalDateX = maxDate - minDate

        if (intervalDateX > 0) {
            val intervalX = (width - offsetX) / intervalDateX

            mTextPaint.textAlign = Paint.Align.CENTER

            repeat(intervalDateX) {
                if (it == intervalDateX - 1) {
                    mTextPaint.textAlign = Paint.Align.RIGHT
                }

                canvas.drawText(
                    (minDate + (it + 1)).toString(),
                    offsetX + (intervalX * (it + 1).toFloat()),
                    height.toFloat(),
                    mTextPaint
                )
            }
        } else {
            canvas.drawText(
                minDate.toString(),
                (width ).toFloat() / 2,
                height.toFloat(),
                mTextPaint
            )
        }
    }

    private fun drawChart(
        canvas: Canvas,
        values: List<Pair<Int, Int>>,
        @ColorInt color: Int,
        offset: Int,
        month: Int
    ) {
        if (values.isEmpty()) return
        resetPaths()

        val count = values.last().first - values.first().first
        val widthUnit = (width.toFloat() - offset) / count

        val heightUnit = height.toFloat() / mState.requireDates().getMaxValueByMonth(month)

        var chartX = 0f + offset
        mLinePath.moveTo(chartX, height - values.first().second.toFloat() * heightUnit)
        values.forEachIndexed { index, value ->
            if (index != 0) {
                chartX += (value.first - values[index - 1].first).toFloat() * widthUnit
                mLinePath.lineTo(chartX, height - value.second.toFloat() * heightUnit)
            } else if (values.size == 1) {
                mLinePath.lineTo(width.toFloat(), height - value.second.toFloat() * heightUnit)
            }
        }
        mLinePath.lineTo(width.toFloat() + 80f, values.last().second.toFloat() * heightUnit)
        mLinePath.lineTo(width.toFloat() + 40f, height.toFloat() + 20f)
        mLinePath.lineTo(0f, height.toFloat() + 20f)
        mLinePath.close()

        mLinePaint.color = color

        mGradientPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            color, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        mGradientPaint.alpha = 0x22

        mClipRect.apply {
            left = offset.toFloat()
            top = 0f
            right = width.toFloat()
            bottom = height.toFloat()
        }
        canvas.save()
        canvas.clipRect(mClipRect)
        canvas.drawPath(mLinePath, mGradientPaint)
        canvas.drawPath(mLinePath, mLinePaint)
        canvas.restore()
    }

    private fun resetPaths() {
        mLinePath.reset()
    }

}