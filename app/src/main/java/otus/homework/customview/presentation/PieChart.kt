package otus.homework.customview.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class PieChart @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    deffStyleAttr: Int = 0,
) : View(context, attr, deffStyleAttr) {

    private companion object {
        const val SAVED_STATE_KEY = "SAVED_STATE"
        const val INSTANCE_STATE_KEY = "INSTANCE_KEY"
    }

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private var onClickActionEvent: ((String) -> Unit)? = null

    private var metaInfo: MetaInfo = MetaInfo(0, 0, 0f)

    private var segments = mutableListOf<Segment>()

    private val defaultSize = (300 * resources.displayMetrics.density).toInt()

    fun setSegments(uiData: PieChartUiInfo) {
        segments.clear()

        var startAngle = 0f

        uiData.spendingInfo.forEach {

            val sweepAngle = 360f * it.amount / it.total

            segments.add(
                Segment(
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    category = it.category,
                    color = it.color,
                    amount = it.amount
                )
            )

            startAngle += sweepAngle
        }

        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {


        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)

        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = max(defaultSize + paddingStart + paddingEnd, wSize)
        val desiredHeight = max(defaultSize + paddingTop + paddingBottom, hSize)

        val measuredWidth = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(wSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }

        val measuredHeight = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(wSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredWidth
        }

        val resultSize = min(measuredHeight, measuredWidth)

        setMeasuredDimension(resultSize, resultSize)
    }

    fun setOnClickEvent(event: (String) -> Unit) {
        this.onClickActionEvent = event
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (segments.isEmpty()) return

        val centerX = width / 2
        val centerY = height / 2

        val radius = minOf(height, width) * 0.4f

        metaInfo.centerX = centerX
        metaInfo.centerY = centerY

        metaInfo.radius = radius

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        segments.forEach { segment ->
            paint.color = segment.color

            canvas.drawArc(
                rect,
                segment.startAngle,
                segment.sweepAngle,
                true,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null && event.action == MotionEvent.ACTION_DOWN) {

            val pieChartIsClicked = isPointInsidePieChart(event.x, event.y)

            if (pieChartIsClicked) {
                val segment = getClickedSegment(event.x, event.y)
                onClickActionEvent?.invoke(segment.category + " ${segment.amount}")
            }
            performClick()

            return true
        }
        return super.onTouchEvent(event)
    }

    private fun isPointInsidePieChart(x: Float, y: Float): Boolean {
        val xLeg = x - metaInfo.centerX
        val yLeg = y - metaInfo.centerY

        val distance = sqrt(xLeg * xLeg + yLeg * yLeg)

        return distance < metaInfo.radius
    }

    private fun getClickedSegment(x: Float, y: Float): Segment {
        val xLeg = x - metaInfo.centerX
        val yLeg = y - metaInfo.centerY

        val result = Segment(0f, 0f, "", 0, 0)

        var angle = Math.toDegrees(atan2(yLeg.toDouble(), xLeg.toDouble()))

        if (angle < 0) angle += 360

        segments.forEach { segment ->
            val endAngle = segment.startAngle + segment.sweepAngle
            if (angle > segment.startAngle && angle <= endAngle) return segment
        }
        return result
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle().apply {
            putParcelable(
                SAVED_STATE_KEY,
                SavedState(segments = this@PieChart.segments, metaInfo = this@PieChart.metaInfo)
            )
            putParcelable(
                INSTANCE_STATE_KEY,
                super.onSaveInstanceState()
            )
        }

        return bundle
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        val savedState = bundle.getParcelable(SAVED_STATE_KEY, SavedState::class.java)
        savedState?.let { saved ->
            metaInfo = saved.metaInfo
        }
        super.onRestoreInstanceState(
            bundle.getParcelable(
                INSTANCE_STATE_KEY,
                Parcelable::class.java
            )
        )
    }

    @Parcelize
    private class Segment(
        val startAngle: Float,
        val sweepAngle: Float,
        val category: String,
        val amount: Int,
        val color: Int
    ) : Parcelable

    @Parcelize
    private class MetaInfo(
        var centerX: Int,
        var centerY: Int,
        var radius: Float
    ) : Parcelable

    @Parcelize
    private data class SavedState(val segments: MutableList<Segment>, val metaInfo: MetaInfo) :
        Parcelable
}