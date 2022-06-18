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

    private var mStringDecorator = LineChartStringDecorator.default()
    private var mState: LineChartState = LineChartState.default()

    private val mLinePaint = Paint().apply {
        // TODO: Перенести в атрибуты
        strokeWidth = 4f
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        pathEffect = CornerPathEffect(80f)
    }
    private val mTextPaint = Paint().apply {
        // TODO: Перенести в атрибуты
        textSize = resources.getDimensionPixelSize(R.dimen.lineChartTextSize).toFloat()
        color = ContextCompat.getColor(context, R.color.axis_info)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val mGradientPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        pathEffect = CornerPathEffect(80f)
    }

    private val mAxisPaint = Paint().apply {
        strokeWidth = 4f
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.axis)
    }

    private val mLinePath = Path()
    private val mClipRect = RectF()

    fun setDecorator(stringDecorator: LineChartStringDecorator) {
        mStringDecorator = stringDecorator
    }

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
        val offsetX = 100
        when (mState) {
            is LineChartState.Dates -> {
                val firstMonth =
                    (mState as LineChartState.Dates).minDate?.get(Calendar.MONTH) ?: return
                val listValues = (mState as LineChartState.Dates).getDatesByMonth(firstMonth)
                drawChart(canvas, listValues, mState.requireDates().color, offsetX)
                drawAxisInfo(canvas, mState.requireDates(), offsetX)
            }
        }
        drawCoordinateAxis(canvas, offsetX)
    }

    private fun drawCoordinateAxis(canvas: Canvas, offsetX: Int) {
        val offsetY = 50f
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
        offsetX: Int
    ) {
        val countY = 4
        val intervalValue = round(state.maxValue * 10f) / 10 / countY
        val intervalY = (height - intervalValue) / countY

        mTextPaint.textAlign = Paint.Align.LEFT

        repeat(countY) {
            canvas.drawText(
                (intervalValue * (it + 1)).toString(),
                10f,
                (height - offsetX) - intervalY * (it + 1),
                mTextPaint
            )
        }

        val minDate = state.minDate?.get(Calendar.DAY_OF_MONTH) ?: return
        val maxDate = state.maxDate?.get(Calendar.DAY_OF_MONTH) ?: return
        val intervalDateX = maxDate - minDate
        val intervalX = (width - offsetX) / intervalDateX

        mTextPaint.textAlign = Paint.Align.CENTER

        repeat(intervalDateX / 2) {
            canvas.drawText(
                (minDate + 2 * (it + 1)).toString(),
                offsetX + (intervalX * 2 * (it + 1).toFloat()),
                height.toFloat(),
                mTextPaint
            )
        }
    }

    private fun drawChart(
        canvas: Canvas,
        values: List<Pair<Int, Int>>,
        @ColorInt color: Int,
        offset: Int
    ) {
        if (values.isEmpty()) return

        val count = values.last().first - values.first().first
        val widthUnit = (width.toFloat() - offset) / count

        val heightUnit = height.toFloat() / mState.requireDates().maxValue

        var chartX = 0f + offset
        mLinePath.moveTo(chartX, values.first().second.toFloat())
        values.forEachIndexed { index, value ->
            if (index != 0) {
                chartX += (value.first - values[index - 1].first).toFloat() * widthUnit
                mLinePath.lineTo(
                    chartX,
                    height - value.second.toFloat() * heightUnit
                )
            }
        }
        mLinePath.lineTo(width.toFloat() + 40f, height.toFloat() + 20f)
        mLinePath.lineTo(0f, height.toFloat() + 20f)
        mLinePath.close()

        mLinePaint.color = color

        mGradientPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            color, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        mGradientPaint.alpha = 0x55

        mClipRect.apply {
            left = offset.toFloat()
            top = offset + 20f
            right = width.toFloat()
            bottom = height.toFloat()
        }
        canvas.save()
        canvas.clipRect(mClipRect)
        canvas.drawPath(mLinePath, mGradientPaint)
        canvas.drawPath(mLinePath, mLinePaint)
        canvas.restore()
    }

}