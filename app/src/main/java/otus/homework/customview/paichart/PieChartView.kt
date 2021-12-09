package otus.homework.customview.paichart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import kotlin.math.*


class PieChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var onClickListener: ((key: String) -> Unit)? = null

    private val strokeWidth: Float = resources.getDimension(R.dimen.pie_chart_stroke_width)

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@PieChartView.strokeWidth
    }

    private val centerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var circleRect = RectF()

    private var circleRadius = 0f

    private var slices: List<Slice> = emptyList()

    private var totalAmount: Int? = null

    private val sliceColors: List<Int> = listOf(
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.BLACK,
        Color.MAGENTA,
        Color.RED,
        Color.YELLOW,
        Color.DKGRAY,
        Color.LTGRAY,
        Color.GRAY
    )

    fun setData(total: Int, data: List<Pair<String, Float>>) {
        var startAngle = 0f
        this.slices = data
            .sortedBy { it.second }
            .mapIndexed { index, pair ->
                Slice(pair.first, pair.second, startAngle, sliceColors[index]).also {
                    startAngle = startAngle.plus(it.sweepAngle)
                }
            }

        this.totalAmount = total

        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).also { state ->
            state.total = totalAmount ?: 0
            state.data = slices.map { Pair(it.name, it.percent) }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            setData(state.total, state.data)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightInSpec = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> width
            MeasureSpec.AT_MOST -> width.coerceAtMost(heightInSpec)
            MeasureSpec.EXACTLY -> heightInSpec
            else -> error("Unreachable")
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val side = min(w, h)

        val left = paddingLeft + strokeWidth / 2f
        val top = paddingTop + strokeWidth / 2f
        val right = side - paddingRight - strokeWidth / 2f
        val bottom = side - paddingBottom - strokeWidth / 2f

        circleRect.set(left, top, right, bottom)
        circleRadius = (circleRect.width() + strokeWidth) / 2
    }


    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || slices.isEmpty()) return

        with(canvas) {
            drawSlice()
            drawCenterText()
        }

    }

    private fun Canvas.drawSlice() {
        slices.forEach {
            drawArc(
                circleRect,
                it.startAngle,
                it.sweepAngle,
                false,
                slicePaint.apply { color = it.color })
        }
    }

    private fun Canvas.drawCenterText() {
        totalAmount.let {
            val x = circleRect.centerX()
            val y = circleRect.centerY()

            centerTextPaint.apply {
                color = Color.BLACK
                textSize = resources.getDimension(R.dimen.pie_chart_center_text_size)
                typeface = Typeface.DEFAULT_BOLD
            }

            drawText(it.toString(), x, y, centerTextPaint)

            val subtext = resources.getString(R.string.pai_chart_center_text)
            val subtextPadding = resources.getDimension(R.dimen.pie_chart_center_text_padding)

            centerTextPaint.apply {
                color = Color.GRAY
                textSize = resources.getDimension(R.dimen.pie_chart_center_subtext_size)
                typeface = Typeface.DEFAULT
            }

            drawText(
                subtext,
                x,
                y.plus(centerTextPaint.textSize).plus(subtextPadding),
                centerTextPaint
            )
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x - circleRect.centerX()
                val y = event.y - circleRect.centerY()

                val r = sqrt(x.pow(2) + y.pow(2))

                if (r in circleRadius.minus(strokeWidth)..circleRadius) {
                    var rAngle = ((atan2(y, x) * 180) / Math.PI)
                    rAngle = if (rAngle < 0) 360 - rAngle.absoluteValue else rAngle

                    val slice = slices.first {
                        rAngle in it.startAngle..it.startAngle + it.sweepAngle
                    }

                    onClickListener?.invoke(slice.name)

                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private inner class Slice(
        val name: String,
        val percent: Float,
        val startAngle: Float,
        val color: Int
    ) {
        val sweepAngle: Float
            get() = percent * 360f / 100f
    }

    private class SavedState : BaseSavedState {

        var data: List<Pair<String, Float>> = emptyList()
        var total: Int = 0

        constructor(parcelable: Parcelable?) : super(parcelable)

        private constructor(parcel: Parcel?) : super(parcel) {
            parcel?.let {
                parcel.readList(data, Pair::class.java.classLoader)
                total = parcel.readInt()
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeList(data)
            parcel.writeInt(total)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }
}