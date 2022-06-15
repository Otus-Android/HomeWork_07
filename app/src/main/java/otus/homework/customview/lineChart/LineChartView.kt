package otus.homework.customview.lineChart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.R
import otus.homework.customview.lineChart.LineChartState.Companion.requireDates
import java.util.*

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
    }
    private val mTextPaint = Paint().apply {
        // TODO: Перенести в атрибуты
        textSize = resources.getDimensionPixelSize(R.dimen.pieChartCenterTextSize).toFloat()
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val mLinePath = Path()

    fun setDecorator(stringDecorator: LineChartStringDecorator) {
        mStringDecorator = stringDecorator
    }

    fun setValue(state: LineChartState) {
        if (state == mState) {
            return
        }

        mState = state

        mLinePaint.pathEffect = CornerPathEffect(80f)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (mState) {
            is LineChartState.Dates -> {
                val firstMonth =
                    (mState as LineChartState.Dates).minDate?.get(Calendar.MONTH) ?: return
                val listValues = (mState as LineChartState.Dates).getDatesByMonth(firstMonth)
                drawDates(canvas, listValues, Color.BLUE)
            }
        }
    }

    private fun drawDates(
        canvas: Canvas,
        values: List<Pair<Int, Int>>,
        @ColorInt color: Int
    ) {
        if (values.isEmpty()) return

        val count = values.last().first - values.first().first
        val widthUnit = width.toFloat() / count

        val heightUnit = height.toFloat() / mState.requireDates().maxValue

        var chartX = 0f
        mLinePath.moveTo(0f, values.first().second.toFloat())
        values.forEachIndexed { index, value ->
            if (index != 0) {
                chartX += (value.first - values[index - 1].first).toFloat() * widthUnit
                mLinePath.lineTo(
                    chartX,
                    height - value.second.toFloat() * heightUnit
                )
            }
        }

        mLinePaint.color = color
        canvas.drawPath(mLinePath, mLinePaint)
    }

}