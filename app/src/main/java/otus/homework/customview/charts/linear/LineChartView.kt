package otus.homework.customview.charts.linear

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.charts.pie.PiePayloadEntity

class LineChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private val axisPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val colors = HashMap<Int, @ColorInt Int>()

    init {
        colors[0] = Color.parseColor("#56e2cf")
        colors[1] = Color.parseColor("#56aee2")
        colors[2] = Color.parseColor("#5668e2")
        colors[3] = Color.parseColor("#8a56e2")
        colors[4] = Color.parseColor("#cf56e2")
        colors[5] = Color.parseColor("#e256ae")
        colors[6] = Color.parseColor("#e25668")
        colors[7] = Color.parseColor("#e28956")
        colors[8] = Color.parseColor("#e2cf56")
        colors[9] = Color.parseColor("#aee256")
    }

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