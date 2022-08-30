package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: PieViewData? = null

    private var initialHeight: Int? = null

    private val borderPaint = Paint()
    private var indicatorCircleRadius = 0f
    private val mainTextPaint = Paint()
    private val oval = RectF()
    private val indicatorCirclePaint = Paint()
    private var bitmap: Bitmap? = null

    var pieChartClickListener: PieChartClickListener? = null

    init {
        borderPaint.apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = Color.WHITE
        }
        mainTextPaint.apply {
            isAntiAlias = true
            color = Color.DKGRAY
        }
        indicatorCirclePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.LTGRAY
        }
    }

    fun setData(data: PieViewData) {
        this.data = data
        setPieSliceDimensions()
        invalidate()
    }

    private fun setPieSliceDimensions() {
        var lastAngle = 0f
        data?.pieSlices?.forEach {
            it.value.startAngle = lastAngle
            it.value.rotationAngle = (((it.value.value / data?.totalValue!!)) * 360f).toFloat()
            it.value.paint.strokeWidth = layoutParams.height / 6F
            lastAngle += it.value.rotationAngle
            setIndicatorLocation(it.key)
        }
    }

    private fun setIndicatorLocation(key: String) {
        data?.pieSlices?.get(key)?.let {
            val middleAngle = it.rotationAngle / 2 + it.startAngle

            it.indicatorCircleLocation.x =
                (layoutParams.height.toFloat() / 2 + layoutParams.height / 12F) *
                        cos(Math.toRadians(middleAngle.toDouble())).toFloat() + width / 2
            it.indicatorCircleLocation.y =
                (layoutParams.height.toFloat() / 2 + layoutParams.height / 12F) *
                        sin(Math.toRadians(middleAngle.toDouble())).toFloat() + layoutParams.height / 2
        }
    }

    private fun setCircleBounds(
        top: Float = 0f, bottom: Float = layoutParams.height.toFloat(),
        left: Float = (width / 2) - (layoutParams.height / 2).toFloat(),
        right: Float = (width / 2) + (layoutParams.height / 2).toFloat()
    ) {
        oval.top = top
        oval.bottom = bottom
        oval.left = left
        oval.right = right
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setCircleBounds()
        setGraphicSizes()
        data?.pieSlices?.forEach {
            setIndicatorLocation(it.key)
        }
    }

    private fun setGraphicSizes() {
        mainTextPaint.textSize = height / 18f
        borderPaint.strokeWidth = height / 80f
        indicatorCircleRadius = height / 70f
    }

    private fun drawIndicators(canvas: Canvas?, pieItem: PieSlice) {
        when {
            pieItem.indicatorCircleLocation.x < width / 2 && pieItem.indicatorCircleLocation.y < height / 2 -> drawIndicatorText(
                canvas,
                pieItem,
                IndicatorAlignment.RIGHT,
                IndicatorAlignment.BOTTOM
            )
            pieItem.indicatorCircleLocation.x > width / 2 && pieItem.indicatorCircleLocation.y > height / 2 -> drawIndicatorText(
                canvas,
                pieItem,
                IndicatorAlignment.LEFT,
                IndicatorAlignment.TOP
            )
            pieItem.indicatorCircleLocation.x < width / 2 && pieItem.indicatorCircleLocation.y > height / 2 -> drawIndicatorText(
                canvas,
                pieItem,
                IndicatorAlignment.RIGHT,
                IndicatorAlignment.TOP
            )
            pieItem.indicatorCircleLocation.x > width / 2 && pieItem.indicatorCircleLocation.y < height / 2 -> drawIndicatorText(
                canvas,
                pieItem,
                IndicatorAlignment.LEFT,
                IndicatorAlignment.BOTTOM
            )
        }
    }

    private fun drawIndicatorText(
        canvas: Canvas?,
        pieItem: PieSlice,
        alignmentX: IndicatorAlignment,
        alignmentY: IndicatorAlignment
    ) {
        val xOffset =
            if (alignmentX == IndicatorAlignment.RIGHT) layoutParams.height / 24F * -1 else layoutParams.height / 24F
        val yOffset =
            if (alignmentY == IndicatorAlignment.BOTTOM) layoutParams.height / 24F * -1 else layoutParams.height / 24F
        if (alignmentX == IndicatorAlignment.LEFT) mainTextPaint.textAlign = Paint.Align.LEFT
        else mainTextPaint.textAlign = Paint.Align.RIGHT
        canvas?.drawText(
            pieItem.category, pieItem.indicatorCircleLocation.x + xOffset,
            pieItem.indicatorCircleLocation.y + yOffset, mainTextPaint
        )
    }

    private fun expandPieSlice(slice: PieSlice) {
        pieChartClickListener?.onClick(slice.category)
        slice.paint.strokeWidth *= 1.5F
        slice.state = PieState.EXPANDED
        requestLayout()
        invalidate()
    }

    private fun collapsePieSlice(slice: PieSlice) {
        slice.paint.strokeWidth = layoutParams.height / 6F
        slice.state = PieState.MINIMIZED
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (initialHeight == null) initialHeight = layoutParams.height
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = bitmap?.let { Canvas(it) }
        data?.pieSlices?.let { slices ->
            slices.forEach {
                canvas?.drawArc(
                    oval,
                    it.value.startAngle,
                    it.value.rotationAngle,
                    false,
                    it.value.paint
                )
                newCanvas?.drawArc(
                    oval,
                    it.value.startAngle,
                    it.value.rotationAngle,
                    false,
                    it.value.paint
                )
                drawIndicators(canvas, it.value)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val color = event?.let { bitmap?.getPixel(event.x.toInt(), event.y.toInt()) }
        if (event?.action == MotionEvent.ACTION_DOWN) return true
        if (event?.action == MotionEvent.ACTION_UP) {
            Log.d("CLICKED", "CLICKED")
            Log.d("CLICKED", color.toString())
            data?.pieSlices?.forEach {
                if (it.value.paint.color == color) {
                    when (it.value.state) {
                        PieState.MINIMIZED -> expandPieSlice(it.value)
                        PieState.EXPANDED -> collapsePieSlice(it.value)
                    }
                } else {
                    collapsePieSlice(it.value)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    enum class IndicatorAlignment {
        LEFT, RIGHT, TOP, BOTTOM
    }
}
