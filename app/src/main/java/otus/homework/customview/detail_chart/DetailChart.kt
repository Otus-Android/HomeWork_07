package otus.homework.customview.detail_chart

import android.content.Context
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.ViewUpdateListener
import otus.homework.customview.asPixel
import otus.homework.customview.detail_chart.model.DatePurchase
import otus.homework.customview.model.Purchase
import kotlin.math.min

class DetailChart @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attr, defStyleAttr), ViewUpdateListener {

    private val chartManager = DetailChartManager(context, this)

    private val defaultSizeHeight = context.asPixel(100)
    private val defaultSizeWidth = context.asPixel(200)

    override fun onRequestInvalidate() {
        invalidate()
    }

    override fun onRequestLayout() {
        requestLayout()
    }

    fun setPayload(payload: List<Purchase>) {
        chartManager.setPayload(payload)
    }

    init {
        isSaveEnabled = true
        minimumWidth = defaultSizeWidth
        minimumHeight = defaultSizeHeight
    }

    override fun onDraw(canvas: Canvas) {
        chartManager.draw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chartManager.onLayout(
            paddingLeft = paddingLeft,
            paddingRight = paddingRight,
            paddingTop = paddingTop,
            paddingBottom = paddingBottom,
            measuredHeight = measuredHeight,
            measuredWidth = measuredWidth
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expectedWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val expectedHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val width = measureDimension(expectedWidth, widthMeasureSpec)
        val height = measureDimension(expectedHeight, heightMeasureSpec)
        if (chartManager.purchases.isEmpty()) {
            setMeasuredDimension(expectedWidth, expectedHeight)
            return
        }
        chartManager.onMeasure()
        setMeasuredDimension(width, height)
    }

    private fun measureDimension(size: Int, measureSpec: Int): Int {
        val measureSize = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> measureSize
            MeasureSpec.AT_MOST -> min(size, measureSize)
            else -> size
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            payload = chartManager.purchases
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val restoredState = state as SavedState
        super.onRestoreInstanceState(restoredState.superState)

        chartManager.updateData(restoredState.payload)
    }

    internal class SavedState : BaseSavedState {

        var payload: List<DatePurchase> = emptyList()

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            source.readList(payload, DatePurchase::class.java.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(payload)
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