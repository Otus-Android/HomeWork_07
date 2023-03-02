package otus.homework.customview.charts.linear

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.charts.pie.PiePayloadEntity

class LineChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {


    private var payload: List<LinePayloadEntry>? = null
    fun updatePayload(payload: List<LinePayloadEntry>){
        this.payload = payload
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                Log.d("LineChartView", "onMeasure UNSPECIFIED")
            }
            MeasureSpec.AT_MOST -> {
                Log.d("LineChartView", "onMeasure AT_MOST")
            }
            MeasureSpec.EXACTLY -> {
                Log.d("LineChartView", "onMeasure EXACTLY")
                //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(widthSize, heightSize)
            }
        }

        //calculateOvalSize()

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d("LineChartView", "onLayout")
    }

}