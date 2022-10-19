package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.pieChart.PieChartSector
import kotlin.math.min

abstract class BaseChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val viewInfo = ViewInfo()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        viewInfo.width = viewWidth
        viewInfo.height = viewHeight

        setMeasuredDimension(viewWidth, viewHeight)
    }

}