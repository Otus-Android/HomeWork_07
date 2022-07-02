package otus.homework.customview.pieChart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import otus.homework.customview.R
import kotlin.math.*

class PieChartView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var mPieChartState: PieChartState = PieChartState.default()
    private var mPreviousSelected: PieChartState.ColorState? = null
    private var mSelected: PieChartState.ColorState? = null
        set(value) {
            if (field != value) {
                mPreviousSelected = field
                field = value
                animateSelection()
            }
        }

    private val mMinSize = resources.getDimensionPixelSize(R.dimen.pieChartMinSize)
    private val mMaxSelectedOffset = resources.getDimensionPixelSize(R.dimen.pieChartSelectedOffset)
    private val mStrokeWidth: Float
    private val mPiePaint = Paint()
    private val mTextPaint = Paint().apply {
        textSize = resources.getDimensionPixelSize(R.dimen.pieChartCenterTextSize).toFloat()
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var mOnSectorSelectListener: (PieChartState.ColorState?) -> Unit = {}

    private var mSize: Int? = null
    private var mPieChartCenter = PointF(0f, 0f)
    private var mOutRadius = 0f
    private var mSelectedOffset = 0
    private var mCenterTextValue: Int? = 0

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.PieChartView, 0, 0
        )

        try {
            mStrokeWidth = typedArray
                .getDimensionPixelSize(R.styleable.PieChartView_chartWidth, 0).toFloat()
        } finally {
            typedArray.recycle()
        }

        mPiePaint.apply {
            strokeWidth = mStrokeWidth
            style = Paint.Style.STROKE
            flags = Paint.ANTI_ALIAS_FLAG
        }
    }

    fun setValue(pieChartState: PieChartState) {
        if (mPieChartState == pieChartState) {
            return
        }

        mPieChartState = pieChartState
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            .takeIf { it >= mMinSize }
            ?: mMinSize
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            .takeIf { it >= mMinSize }
            ?: mMinSize

        if (widthSize > heightSize) {
            widthSize = heightSize
        } else {
            heightSize = widthSize
        }

        mSize = widthSize.takeIf { it <= heightSize } ?: heightSize

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPieChart(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            findSector(event)
            invalidate()
        }

        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState())
        bundle.putSerializable(STATE, mPieChartState)
        bundle.putSerializable(SELECTED, mSelected)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var restoredState = state
        if (state is Bundle) {
            mPieChartState = state.getSerializable(STATE) as? PieChartState
                ?: PieChartState.default()
            mSelected = state.getSerializable(SELECTED) as? PieChartState.ColorState

            restoredState = state.getParcelable(SUPER_STATE)
        }
        super.onRestoreInstanceState(restoredState)
    }

    private fun findSector(event: MotionEvent) {
        var startAngle = 0f * PI.toFloat() / 180
        var endAngle: Float
        val distance = sqrt(
            (mPieChartCenter.x - event.x).toDouble().pow(2.0) +
                    (mPieChartCenter.y - event.y).toDouble().pow(2.0)
        )
        val inRadius = mOutRadius - mStrokeWidth
        if (distance > mOutRadius || distance < inRadius) {
            mSelected = null
            mOnSectorSelectListener(null)
            return
        }

        val startSegmentX = mPieChartCenter.x
        val startSegmentY = mPieChartCenter.y - mOutRadius
        val vector1 = Pair(startSegmentX - mPieChartCenter.x, startSegmentY - mPieChartCenter.y)
        val vector2 = Pair(event.x - mPieChartCenter.x, event.y - mPieChartCenter.y)
        val cosBetweenVectors = (vector1.first * vector2.first + vector1.second * vector2.second) /
                (sqrt(vector1.first.pow(2) + vector1.second.pow(2)) *
                        sqrt(vector2.first.pow(2) + vector2.second.pow(2)))

        mPieChartState.colorStates.forEach { colorState ->
            endAngle =
                startAngle + 360 * mPieChartState.getPart(colorState.value) * PI.toFloat() / 180

            val clickedAngle = when (event.x < mPieChartCenter.x) {
                true -> PI.toFloat() * 2 - acos(cosBetweenVectors)
                false -> acos(cosBetweenVectors)
            }
            if (clickedAngle in startAngle..endAngle) {
                mSelected = colorState
                mOnSectorSelectListener(colorState)
                return
            }

            startAngle = endAngle
        }
    }

    fun setOnSectorSelectListener(onSelect: (state: PieChartState.ColorState?) -> Unit) {
        mOnSectorSelectListener = onSelect
    }

    private fun drawPieChart(canvas: Canvas) {
        val size = mSize ?: return
        val left = 0f + mStrokeWidth / 2 + paddingLeft
        val right = width.toFloat() - mStrokeWidth / 2 - paddingRight
        val top = 0f + mStrokeWidth / 2 + paddingTop
        val bottom = size.toFloat() - mStrokeWidth / 2 - paddingBottom
        var startAngle = START_ANGLE

        mPieChartState.colorStates.forEach { colorState ->
            val selectedOffset = getSelectedOffset(colorState)
            mPiePaint.strokeWidth = mStrokeWidth + selectedOffset * 2

            val separatorSweepAngle = drawSeparator(
                canvas, left, top, right, bottom, selectedOffset, startAngle
            )

            startAngle += separatorSweepAngle
            startAngle += drawPart(
                canvas, left, top, right, bottom, selectedOffset,
                startAngle, colorState, separatorSweepAngle
            )
        }

        drawCenterText(canvas, left, top, right, bottom)
    }

    private fun getSelectedOffset(colorState: PieChartState.ColorState) = when {
        colorState.id == mSelected?.id -> mSelectedOffset
        colorState.id == mPreviousSelected?.id -> mMaxSelectedOffset - mSelectedOffset
        else -> 0
    }

    private fun drawSeparator(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        selectedOffset: Int,
        startAngle: Float
    ): Float {
        val separatorSweepAngle = 0.5f
        mPiePaint.color = Color.WHITE
        canvas.drawArc(
            left - selectedOffset,
            top - selectedOffset,
            right + selectedOffset,
            bottom + selectedOffset,
            startAngle,
            separatorSweepAngle,
            false,
            mPiePaint
        )
        return separatorSweepAngle
    }

    private fun drawPart(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        selectedOffset: Int,
        startAngle: Float,
        colorState: PieChartState.ColorState,
        separatorSweepAngle: Float
    ): Float {
        mPiePaint.color = colorState.color.toInt()
        val sweepAngle = 360 * mPieChartState.getPart(colorState.value) - separatorSweepAngle

        canvas.drawArc(
            left - selectedOffset,
            top - selectedOffset,
            right + selectedOffset,
            bottom + selectedOffset,
            startAngle,
            sweepAngle,
            false,
            mPiePaint
        )
        return sweepAngle
    }

    private fun drawCenterText(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val text = when (mCenterTextValue) {
            null -> DEFAULT_CENTER_TEXT
            else -> "${round(mPieChartState.getPart(mCenterTextValue!!) * 1000) / 10}%"
        }

        val centerX = left + (right - left) / 2
        val centerY = top + (bottom - top) / 2

        canvas.drawText(text, centerX, centerY + mStrokeWidth / 2, mTextPaint)
        mPieChartCenter = PointF(centerX, centerY)
        mOutRadius = (right - left) / 2 + mStrokeWidth / 2
    }

    private fun animateSelection() {
        val animationDuration = 200L
        ValueAnimator.ofInt(0, mMaxSelectedOffset).apply {
            duration = animationDuration
            interpolator = AccelerateInterpolator()
            addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Int
                mSelectedOffset = animatedValue
                val value = mSelected?.value ?: 0
                val previousValue = mPreviousSelected?.value ?: 0
                mCenterTextValue = (previousValue + (animatedValue.toFloat() / mMaxSelectedOffset) *
                        (value - previousValue)).toInt()
                invalidate()
            }
        }.start()
    }

    companion object {
        private const val DEFAULT_CENTER_TEXT = "-"
        private const val START_ANGLE = -90f
        private const val SUPER_STATE = "superState"
        private const val STATE = "state"
        private const val SELECTED = "selected"
    }

}