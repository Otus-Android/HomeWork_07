package otus.homework.customview.pie_chart

import android.content.Context
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.ViewUpdateListener
import otus.homework.customview.pie_chart.model.ChartState
import otus.homework.customview.pie_chart.model.PieConfigViewData
import kotlin.math.min

class ChartPie @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attr, defStyleAttr), ViewUpdateListener {

    private val chartManager = PieChartManager(this)

    init {
        isSaveEnabled = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        minimumWidth = chartManager.chartModel.minSizePx
        minimumHeight = chartManager.chartModel.minSizePx
    }

    fun config(configViewData: PieConfigViewData) {
        chartManager.config(configViewData)
    }

    override fun onRequestInvalidate() {
        invalidate()
    }

    override fun onRequestLayout() {
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expectedWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val expectedHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val width = measureDimension(expectedWidth, widthMeasureSpec)
        val height = measureDimension(expectedHeight, heightMeasureSpec)
        val exactlySize = min(width, height)
        setMeasuredDimension(exactlySize, exactlySize)
    }

    private fun measureDimension(size: Int, measureSpec: Int): Int {
        val measureSize = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> measureSize
            MeasureSpec.AT_MOST -> min(size, measureSize)
            else -> size
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chartManager.onLayout(
            paddingLeft,
            paddingRight,
            paddingTop,
            paddingBottom,
            measuredHeight,
            measuredWidth
        )
    }

    override fun onDraw(canvas: Canvas) {
        chartManager.drawController.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return chartManager.onTouch(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            val state = ChartState(
                listCategory = chartManager.chartModel.drawData,
                selectedCategory = chartManager.currentAnimatedCategoryMax
            )
            payload = state
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val restoredState = state as SavedState
        super.onRestoreInstanceState(restoredState.superState)

        chartManager.updateData(restoredState.payload)
    }

    internal class SavedState : BaseSavedState {
        var payload: ChartState? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            source.readParcelable<ChartState>(ChartState::class.java.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(payload, PARCELABLE_WRITE_RETURN_VALUE)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}