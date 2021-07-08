package otus.homework.customview.pie_chart

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.ext.dPToPx
import kotlin.properties.Delegates

class PayChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val colors: MutableList<Int> = mutableListOf()
    private var defaultWidth = 40
        get() = field.dPToPx()
    private var defaultHeight = 40
        get() = field.dPToPx()
    private var circleWidth: Float? = null
        get() = field?.dPToPx()
    private var defaultCircleWidth: Float = 10f
        get() = field.dPToPx()
    private var segments: List<Pair<Path, Paint>>? = null

    var payments: List<Payment>? by Delegates.observable(null) { _, _, newValue ->
        newValue?.let {
            segments = fillSegments(it)
            invalidate()
        }
    }

    init {
        isSaveEnabled = true
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PayChartView)
        try {
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_1, Color.BLUE))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_2, Color.GREEN))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_3, Color.RED))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_4, Color.GRAY))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_5, Color.MAGENTA))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_6, Color.CYAN))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_7, Color.YELLOW))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_8, Color.BLUE))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_9, Color.GREEN))
            colors.add(typedArray.getColor(R.styleable.PayChartView_color_10, Color.RED))
            circleWidth = typedArray.getDimension(R.styleable.PayChartView_circle_width, defaultCircleWidth)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize < defaultWidth) widthSize = defaultWidth
        if (heightSize < defaultHeight) heightSize = defaultHeight

        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST ->
                setMeasuredDimension(defaultWidth, defaultHeight)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST ->
                setMeasuredDimension(widthSize, widthSize)
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY ->
                setMeasuredDimension(heightSize, heightSize)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && widthSize != heightSize -> {
                if (widthSize >= heightMode) setMeasuredDimension(heightSize, heightSize)
                else setMeasuredDimension(widthSize, widthSize)
            }
            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        segments?.forEach {
            canvas?.drawPath(it.first, it.second)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        superState?.let {
            val state = SavedState(superState)
            state.items = payments
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                payments = state.items

                requestLayout()
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    private fun fillSegments(items: List<Payment>): List<Pair<Path, Paint>> {
        val oval = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val paymentsSum = items.map { it.amount }.sum()
        var angle = 0f
        var colorIndex = 0
        return items.map { payment ->
            val paint = Paint()
            val path = Path()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = circleWidth ?: defaultCircleWidth
            if (colorIndex == colors.size) colorIndex = 0
            paint.color = colors[colorIndex]
            val deltaAngle = payment.amount / paymentsSum * 360
            path.addArc(oval, angle, angle + deltaAngle)
            path.close()
            angle += deltaAngle
            colorIndex++
            Pair(path, paint)
        }
    }

}