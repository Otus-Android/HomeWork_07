package otus.homework.customview.view.piechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import otus.homework.customview.Expense
import otus.homework.customview.R
import otus.homework.customview.view.PaintGenerator.getPaint
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var onSliceClick: ((String) -> Unit)? = null

    private var size = MIN_SIZE

    private val pieChartData = PieData()
    private val oval = RectF()

    private val arcWidth = context.resources
        .getDimensionPixelSize(R.dimen.pie_chart_stroke_width).toFloat()


    fun setData(expenseList: List<Expense>) {
        expenseList.forEach {
            pieChartData.add(
                name = it.category,
                value = it.amount.toFloat(),
            )
        }

        setPieSliceDimensions()
        invalidate()
    }

    private fun setPieSliceDimensions() {
        var lastAngle = 0f
        pieChartData.pieSlices.forEach {
            it.value.startAngle = lastAngle
            it.value.sweepAngle =
                (((it.value.value / pieChartData.totalValue)) * 360f).toFloat() - ARC_SPACE
            lastAngle += it.value.sweepAngle + ARC_SPACE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = calculateSize(widthMeasureSpec)
        val heightSize = calculateSize(heightMeasureSpec)

        size = min(widthSize, heightSize)
        oval.apply {
            top = arcWidth / 2
            bottom = size.toFloat() - arcWidth / 2
            left = arcWidth / 2
            right = size.toFloat() - arcWidth / 2
        }
        setMeasuredDimension(size, size)
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> MIN_SIZE
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> max(MIN_SIZE, size)
            else -> throw IllegalStateException("Invalid MeasureSpec mode")
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        pieChartData.pieSlices.forEach {
            canvas.drawArc(
                oval,
                it.value.startAngle,
                it.value.sweepAngle,
                false,
                getPaint(it.value.paintIndex)
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle().apply {
            putInt(PAINT_INDEX_TAG, pieChartData.paintIndex)
            putSerializable(PIE_SLICES_TAG, pieChartData.pieSlices)
            putDouble(TOTAL_VALUE_TAG, pieChartData.totalValue)
            putParcelable(BUNDLE_TAG, super.onSaveInstanceState())
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            pieChartData.paintIndex = state.getInt(PAINT_INDEX_TAG)
            pieChartData.totalValue = state.getDouble(TOTAL_VALUE_TAG)
            pieChartData.pieSlices =
                state.getSerializable(PIE_SLICES_TAG) as HashMap<String, PieSlice>

            super.onRestoreInstanceState(state.getParcelable(BUNDLE_TAG))
        }
    }

    fun setOnSliceClick(listener: (String) -> Unit) {
        onSliceClick = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.onClick {
            getTouchPieChartAngle(event.x, event.y)?.let { angle ->
                for (item in pieChartData.pieSlices.values) {
                    if (item.startAngle < angle && (item.startAngle + item.sweepAngle) >= angle) {
                        onSliceClick?.invoke(item.name)
                        break
                    }
                }
            }
        }

        return true
    }

    private fun getTouchPieChartAngle(touchedX: Float, touchedY: Float): Float? {
        val outerRadius = size / 2
        val innerRadius = outerRadius - arcWidth
        val centerX = size / 2
        val centerY = size / 2

        val distance = sqrt((centerX - touchedX).pow(2) + (centerY - touchedY).pow(2))

        if (distance < innerRadius || distance > outerRadius) {
            return null
        }

        val angle = Math.toDegrees(
            atan2(
                (centerY - touchedY).toDouble(),
                (centerX - touchedX).toDouble()
            )
        )

        return angle.toFloat() + 180
    }

    private inline fun MotionEvent.onClick(callback: () -> Unit): MotionEvent {
        when (actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> callback()
        }
        return this
    }

    companion object {
        private const val MIN_SIZE = 300
        private const val ARC_SPACE = 1F

        private const val PAINT_INDEX_TAG = "paint_index_tag"
        private const val PIE_SLICES_TAG = "pie_slices_tag"
        private const val TOTAL_VALUE_TAG = "total_value_tag"
        private const val BUNDLE_TAG = "bundle_tag"
    }
}
