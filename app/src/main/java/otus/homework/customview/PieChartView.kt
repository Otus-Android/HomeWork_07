package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.parcelize.Parcelize
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val bounds = RectF()

    private val slices = ArrayList<PieData>()
    private val slicePaint = Paint().apply {
        isAntiAlias = true
    }

    var onClicked: ((String) -> Unit)? = null

    fun setData(categories: List<ExpenseCategory>) {
        var startAngle = 0f
        val total = categories.sumOf { it.totalAmount }

        for (category in categories) {
            val angle = (category.totalAmount / total.toFloat()) * 360.0f

            slices.add(
                PieData(
                    category.color,
                    category.title,
                    category.totalAmount,
                    startAngle,
                    angle
                )
            )

            startAngle += angle
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val squareSize = min(widthSize, heightSize)

        setMeasuredDimension(squareSize, squareSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        bounds.top = paddingTop.toFloat()
        bounds.left = paddingLeft.toFloat()
        bounds.right = (right - left).toFloat() - paddingRight
        bounds.bottom = (bottom - top).toFloat() - paddingBottom
    }

    override fun onDraw(canvas: Canvas) {
        for (slice in slices) {
            slicePaint.color = slice.color

            canvas.drawArc(
                bounds,
                slice.startAngle, slice.sweepAngle,
                true,
                slicePaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            val normalized = normalizeCoordinates(x, y)
            val slice = getSliceAt(normalized)

            if (slice != null) {
                onClicked?.invoke(slice.label)
            }
        }

        return false
    }

    private fun getSliceAt(point: PointF): PieData? {
        val length = point.length()
        val radius = bounds.height() / 2f

        if (length > radius) {
            return null
        }

        val angleDegrees = getAngle(point)
        val angleReversed = 360f - angleDegrees

        return slices.find {
            val start = it.startAngle
            val end = it.startAngle + it.sweepAngle

            angleReversed in start..end
        }
    }

    private fun getAngle(point: PointF): Double {
        val angleRadians = atan2(point.y, point.x)
        val angleDegrees = (angleRadians * (180f / PI))

        return if (angleDegrees < 0) angleDegrees + 360
        else angleDegrees
    }

    private fun normalizeCoordinates(x: Float, y: Float): PointF {
        val centerX = width / 2f
        val centerY = height / 2f

        val normalizedX = x - centerX
        val normalizedY = centerY - y

        return PointF(normalizedX, normalizedY)
    }

    override fun onSaveInstanceState(): Parcelable {
        return State(super.onSaveInstanceState(), slices)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is State) {
            super.onRestoreInstanceState(state.superState)
            slices.addAll(state.data)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    @Parcelize
    private data class PieData(
        val color: Int,
        val label: String,
        val count: Int,
        val startAngle: Float,
        val sweepAngle: Float,
    ) : Parcelable

    @Parcelize
    private class State(
        private val baseState: Parcelable?,
        val data: List<PieData>,
    ) : BaseSavedState(baseState)
}
