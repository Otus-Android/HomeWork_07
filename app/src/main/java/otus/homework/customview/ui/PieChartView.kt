package otus.homework.customview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import otus.homework.customview.Either
import otus.homework.customview.entities.Arc
import otus.homework.customview.entities.Category
import otus.homework.customview.entities.ErrorResult
import otus.homework.customview.failure
import otus.homework.customview.success
import java.lang.Integer.max
import java.lang.Math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

class PieChartView : View {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val categories: MutableList<Category> = mutableListOf()
    var isFragmentStateSaved = false

    private var pieRadius by Delegates.notNull<Float>()
    private var blankRadius by Delegates.notNull<Float>()
    private var clickListener: ((Category) -> Unit)? = null

    private val arcs = mutableListOf<Arc>()
    private val animatedArcs = mutableListOf<Arc>()
    private var currentArc: Arc? = null
    private val rectForDraw = RectF()

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val blankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = mapDpInPixels(5f)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return if (isFragmentStateSaved) PieChartViewSavedState(arcs, super.onSaveInstanceState())
        else super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PieChartViewSavedState && isFragmentStateSaved) {
            super.onRestoreInstanceState(state.superState)
            arcs.clear()
            arcs.addAll(state.arcs)
        } else super.onRestoreInstanceState(state)
    }

    fun initView(list: List<Category>) {
        updateCategories(list)
        initArcs()
    }

    private fun updateCategories(list: List<Category>) {
        categories.clear()
        categories.addAll(list)
    }

    fun setOnClickListener(listener: (Category) -> Unit) {
        clickListener = listener
    }

    private fun initRectForDraw() {
        val safeWidth = width - 2 * mapDpInPixels(HORIZONTAL_PADDING)
        val safeHeight = height - 2 * mapDpInPixels(VERTICAL_PADDING)
        val centerX = width / 2f
        val centerY = height / 2f
        pieRadius = min(safeWidth / 2f, safeHeight / 2f)
        blankRadius = pieRadius * BLANK_RADIUS_IN_PROPORTION

        rectForDraw.left = centerX - pieRadius
        rectForDraw.right = centerX + pieRadius
        rectForDraw.top = centerY - pieRadius
        rectForDraw.bottom = centerY + pieRadius
    }

    private fun initArcs() {
        val totalFromAllCategories = categories.map { it.total }
            .fold(0) { total, item -> total + item }
        val unitTotalInDegree = 2 * PI_IN_DEGREES / totalFromAllCategories.toFloat()
        var currentAngle = START_ANGLE
        categories.forEach {
            val sweepAngle = it.total * unitTotalInDegree
            val arc = Arc(
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                category = it
            )
            arcs.add(arc)
            currentAngle += sweepAngle
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> updateCurrentArc(event)
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }


    private fun updateCurrentArc(event: MotionEvent) {
        currentArc = when (val arcEvent = findArc(event.x, event.y)) {
            is Either.Success -> arcEvent.result
            is Either.Failure -> null
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        currentArc?.let { clickListener?.invoke(it.category) }
        return true
    }

    private fun findArc(x: Float, y: Float): Either<ErrorResult.UnknownArc, Arc> {
        val dx = x - rectForDraw.centerX()
        val dy = y - rectForDraw.centerY()
        val polarRadius =
            sqrt((rectForDraw.centerX() - x).pow(2) + (rectForDraw.centerY() - y).pow(2))
        if (polarRadius > pieRadius || polarRadius < blankRadius)
            return ErrorResult.UnknownArc.failure()
        val theta = atan2(dy, dx) / PI * PI_IN_DEGREES
        val polarAngle = if (theta < 0) 2 * PI_IN_DEGREES + theta else theta
        val arc = arcs.find {
            it.startAngle < polarAngle && (it.startAngle + it.sweepAngle) >= polarAngle
        }
        return arc?.success() ?: ErrorResult.UnknownArc.failure()
    }

    fun runAnimation() {
        if (arcs.size == 0) return
        animatedArcs.clear()
        var currentArc = arcs[0]
        animatedArcs.add(currentArc)
        val arrayAngle = FloatArray(360 * 5) { it / 5.toFloat() }
        ValueAnimator.ofFloat(*arrayAngle).apply {
            interpolator = AccelerateInterpolator()
            duration = 500
            addUpdateListener { value ->
                val angle = value.animatedValue as Float
                val arc =
                    arcs.find { it -> it.startAngle <= angle && (it.startAngle + it.sweepAngle) > angle }
                arc?.let { it ->
                    if (it.category != currentArc.category) {
                        animatedArcs.removeLast()
                        animatedArcs.add(currentArc)
                        currentArc = it
                        animatedArcs.add(currentArc)
                    }
                    animatedArcs.removeLast()
                    animatedArcs.add(it.copy(sweepAngle = angle - it.startAngle))
                }
                invalidate()
            }
            start()
            doOnEnd { correctErrorOfInterpolator() }
        }
    }

    private fun correctErrorOfInterpolator() {
        animatedArcs.last().sweepAngle = 360f - animatedArcs.last().startAngle
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || arcs.size == 0) return
        drawArcs(canvas)
        drawBlankCircle(canvas)
    }

    private fun drawArcs(canvas: Canvas) {
        animatedArcs.forEach {
            drawArc(canvas, it, arcPaint.apply { color = it.category.color })
            drawArc(canvas, it, strokePaint)
        }
    }

    private fun drawArc(canvas: Canvas, arc: Arc, paint: Paint) {
        canvas.drawArc(
            rectForDraw,
            arc.startAngle,
            arc.sweepAngle,
            true,
            paint
        )
    }

    private fun drawBlankCircle(canvas: Canvas) {
        canvas.drawCircle(
            rectForDraw.centerX(),
            rectForDraw.centerY(),
            blankRadius,
            blankPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paddingWidth = paddingStart + paddingEnd
        val paddingHeight = paddingTop + paddingBottom
        val minWidth = suggestedMinimumWidth + paddingWidth
        val minHeight = suggestedMinimumHeight + paddingHeight
        val desiredPieDiameterInPixels = mapDpInPixels(DESIRED_PIE_DIAMETER_IN_DP).toInt()
        val desiredWidth = max(minWidth, desiredPieDiameterInPixels + paddingWidth)
        val desiredHeight = max(minHeight, desiredPieDiameterInPixels + paddingHeight)
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    private fun mapDpInPixels(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initRectForDraw()
        runAnimation()
    }

    companion object {

        private const val DESIRED_PIE_DIAMETER_IN_DP = 200f
        private const val PI_IN_DEGREES = 180
        private const val HORIZONTAL_PADDING = 15f
        private const val VERTICAL_PADDING = 15f
        private const val BLANK_RADIUS_IN_PROPORTION = 0.5f
        private const val START_ANGLE = 0f
    }
}