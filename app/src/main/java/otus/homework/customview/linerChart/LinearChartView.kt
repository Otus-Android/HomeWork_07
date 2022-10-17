package otus.homework.customview.linerChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import otus.homework.customview.BaseChartView
import otus.homework.customview.pieChart.PieChartSector

class LinearChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseChartView(context, attrs) {

    private var chartState = LineChartState()

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL_AND_STROKE
        color = Color.BLACK
    }

    override fun drawChartParts(data: List<PieChartSector>) {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(
            viewInfo.getCenterX().toFloat(),
            viewInfo.getCenterY().toFloat(),
            200f,
            paint
        )
    }


    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return BaseSavedState(chartState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        chartState = (state as BaseSavedState).superState as LineChartState
        super.onRestoreInstanceState(state)
    }
}