package otus.homework.customview.pie_chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.ext.dPToPx
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class PayChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val colors: MutableList<Int> = mutableListOf()
    private var defaultWidth = 40
        get() = field.dPToPx()
    private var defaultHeight = 40
        get() = field.dPToPx()
    private var circleWidth: Float? = null
        get() = field?.dPToPx()
    private var defaultCircleWidth: Float = 20f
        get() = field.dPToPx()
    private var segments: Pair<RectF, List<Triple<Float, Float, Paint>>>? = null
    private var paymentCategories: List<Category>? = null

    var onChartClick: ((Category) -> (Unit))? = null

    init {
        isSaveEnabled = true
        isClickable = true
        isFocusable = true
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

    fun setPayments(payments: List<Payment>) {
        val items = mutableListOf<Category>()
        payments.map { it.category }.distinct().forEachIndexed { index, category ->
            val categoryPayments = payments.filter { it.category == category }
            items.add(Category(index, categoryPayments))
        }
        paymentCategories = items
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        paymentCategories?.let {
            fillSegments(it)
            invalidate()
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        segments?.second?.forEach {
            canvas?.drawArc(segments!!.first, it.first, it.second, false, it.third)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        superState?.let {
            val state = SavedState(superState)
            state.items = paymentCategories
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                paymentCategories = state.items

                requestLayout()
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                checkPushPoint(event.x, event.y)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun fillSegments(items: List<Category>) {
        val strokeWidth = circleWidth ?: defaultCircleWidth
        val oval = RectF(strokeWidth / 2f, strokeWidth / 2f, width.toFloat() - strokeWidth / 2f, height.toFloat() - strokeWidth / 2f)
        val paymentsSum = items.sumOf { it.sum }
        var angle = 0f
        var colorIndex = 0
        val anglePaintItems = mutableListOf<Triple<Float, Float, Paint>>()
        items.forEach { category ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            if (colorIndex == colors.size) colorIndex = 0
            paint.color = colors[colorIndex]
            val deltaAngle = category.sum.toFloat() / paymentsSum.toFloat() * 360f
            anglePaintItems.add(Triple(angle, deltaAngle, paint))
            angle += deltaAngle
            colorIndex++
        }
        segments = Pair(oval, anglePaintItems)
    }

    private fun checkPushPoint(x: Float, y: Float) {
        val strokeWidth = circleWidth ?: defaultCircleWidth
        val smallRadius = (width.toFloat() - 2f * strokeWidth) / 2f
        val bigRadius = width.toFloat() / 2f
        val deltaX = x - bigRadius
        val deltaY = y - bigRadius
        val hypotenuse = sqrt(deltaX.pow(2) + deltaY.pow(2))
        val isInRadius = hypotenuse in smallRadius..bigRadius
        if (!isInRadius) return
        val angle = atan2(deltaY, deltaX) * 180 / PI
        val deltaAngle = if (deltaY < 0) 360 else 0
        val paymentIndex = segments?.second?.indexOfFirst {
            angle + deltaAngle >= it.first && angle + deltaAngle <= it.first + it.second
        }
        if (paymentIndex == null || paymentIndex == -1 || paymentCategories == null) return
        onChartClick?.invoke(paymentCategories!![paymentIndex])
    }

}