package otus.homework.customview.chartview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import kotlin.math.*

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : View(context, attrs, defStyleAttrs) {

    private var categoryClickListener: OnCategoryClickListener? = null

    fun setOnCategoryClickListener(listener: OnCategoryClickListener) {
        categoryClickListener = listener
    }

    /**
     * Changes state when user click in the center of the pieChart.
     * When in [MINIMIZED] form, no labels are drawn.
     * When in [EXPANDED] forms, category labels are drawn.
     */
    private var pieState: PieChartState = PieChartState.MINIMIZED

    /**
     * Needed to track [EXPANDED] - [MINIMIZED] states of the chart
     */
    private var initialHeight: Int? = null

    /**
     * Holds the view.
     */
    private val pieRect = RectF()

    private val density = resources.displayMetrics.density

    private val defaultSize =
        (resources.getDimension(R.dimen.pie_chart_default_size)).toInt()

    /**
     * Radius inside the pieChart where total amount is printed.
     */
    private var innerRadius = 0f

    /**
     * Radius of the pieChart itself.
     */
    private var outerRadius = 0f

    /**
     * Radius of the circle that points to the category name.
     */
    private var categoryNameCircleRadius =
        resources.getDimension(R.dimen.pie_chart_category_name_circle_radius)

    /**
     * Margin between the pieChart and the view boundaries.
     */
    private var pieMargin = 0f

    /**
     * Width of the pie slice.
     */
    private var sliceWidth = 0f

    /**
     * Paint used to draw separators between slices.
     */
    private val separatorsPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.pie_chart_separator_width)
        color = Color.WHITE
    }

    /**
     * Paint used to draw lines going out of the pie slices to either left of the pie
     * chart or right of the pie chart depending on the side where the slice is
     * locates. Category name is drawn above the paint.
     */
    private val categoryLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.teal_700)
        strokeWidth = resources.getDimension(R.dimen.pie_chart_line_width)
        alpha = 0
    }

    /**
     * Paint used to draw names of categories.
     */
    private val categoryNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.pie_chart_category_text_size)
        color = Color.BLACK
        alpha = 0
    }

    /**
     * Paint used to draw circles from which category lines start.
     */
    private val categoryCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.teal_700)
        alpha = 0
    }

    /**
     * Paint used to draw the total amount of the pie chart data.
     */
    private val totalAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.pie_chart_total_amount_text_size)
        color = Color.BLACK
    }

    /**
     * Color inside the pieChart.
     */
    private var innerPieBackgroundColor = Color.WHITE

    /**
     * Sets the color of the inner pie circle.
     */
    fun setInnerPieBackground(@ColorRes color: Int) {
        innerPieBackgroundColor = color
        invalidate()
    }

    /**
     * Paint used to draw the circle inside the pie chart. That circle contains
     * total amount.
     */
    private val innerPiePaint = Paint().apply {
        style = Paint.Style.FILL
        color = innerPieBackgroundColor
    }

    /**
     * Data that is used to fill pieChart slices.
     */
    private var pieData: PieData? = null

    // animation
    private val expandAnimator = ValueAnimator.ofInt()
    private val collapseAnimator = ValueAnimator.ofInt()
    private val categoryNameAnimator = ValueAnimator.ofInt()
    private val totalAmountAnimator = ValueAnimator.ofInt()
    private val animateExpansion = AnimatorSet()
    private val animateCollapse = AnimatorSet()


    init {
        setupAnimation()
    }

    private fun setupAnimation() {
        expandAnimator.apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                layoutParams.height = it.animatedValue as Int
                requestLayout()
                setPieChartRectDimensions()
                setDimensionsForSlices()
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    pieState = PieChartState.EXPANDED
                }
            })
        }

        collapseAnimator.apply {
            duration = 200
            interpolator = LinearInterpolator()
            addUpdateListener {
                layoutParams.height = it.animatedValue as Int
                requestLayout()
                setPieChartRectDimensions()
                setDimensionsForSlices()
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    pieState = PieChartState.MINIMIZED
                }
            })
        }

        categoryNameAnimator.apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                categoryNamePaint.alpha = it.animatedValue as Int
                categoryLinePaint.alpha = it.animatedValue as Int
                categoryCirclePaint.alpha = it.animatedValue as Int
                invalidate()
            }
        }

        totalAmountAnimator.apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                totalAmountPaint.alpha = it.animatedValue as Int
                invalidate()
            }
        }

        animateCollapse
            .play(collapseAnimator)
            .with(categoryNameAnimator)
            .with(totalAmountAnimator)

        animateExpansion
            .play(expandAnimator)
            .with(categoryNameAnimator)
            .with(totalAmountAnimator)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val size: Int
        when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                size = heightSpecSize.coerceAtMost(widthSpecSize)
            }
            widthMode == MeasureSpec.EXACTLY -> {
                size = widthSpecSize.coerceAtMost(heightSpecSize)
            }
            heightMode == MeasureSpec.EXACTLY -> {
                size = heightSpecSize.coerceAtMost(widthSpecSize)
            }
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED -> {
                size = defaultSize
            }
            widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST -> {
                size = defaultSize.coerceAtMost(min(heightSpecSize, widthSpecSize))
            }
            else -> {
                size = defaultSize.coerceAtMost(min(heightSpecSize, widthSpecSize))
            }
        }

        pieMargin = size / 6f
        outerRadius = size / 2 - pieMargin
        sliceWidth = outerRadius / 3
        innerRadius = outerRadius - sliceWidth

        if (initialHeight == null) {
            initialHeight = size
        }

        setMeasuredDimension(size, size)
    }

    fun setData(data: PieData?) {
        this.pieData = data
        setDimensionsForSlices()
        requestLayout()
        invalidate()
    }

    /**
     * Calculate dimension for pieChart slices.
     */
    private fun setDimensionsForSlices() {
        var lastAngle = 0f
        pieData?.pieModels?.forEach { entry ->
            entry.value.startAngle = lastAngle
            entry.value.sweepAngle =
                (((entry.value.amount / pieData?.totalAmount!!)) * 360f).toFloat()
            lastAngle += entry.value.sweepAngle

            setCategoryNameLocation(entry.key)
        }
    }

    /**
     * Use the angle between the start and sweep angles to help get position of the
     * indicator circle.
     *
     * formula for x pos: (length of line) * cos(middleAngle) +
     *                    (distance from left edge of screen)
     *
     * formula for y pos: (length of line) * sin(middleAngle) +
     *                    (distance from top edge of screen)
     *
     * @param category key of pie slice being altered
     */
    private fun setCategoryNameLocation(category: String) {
        pieData?.pieModels?.get(category)?.let { pieModel ->
            val middleAngle = (pieModel.sweepAngle / 2 + pieModel.startAngle).toDouble()

            val lengthOfLine = outerRadius - (sliceWidth / 2)

            pieModel.categoryLocation.x =
                lengthOfLine * cos(Math.toRadians(middleAngle)).toFloat() +
                        measuredWidth / 2

            pieModel.categoryLocation.y =
                lengthOfLine * sin(Math.toRadians(middleAngle)).toFloat() +
                        measuredHeight / 2
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setPieChartRectDimensions()

        pieData?.pieModels?.forEach { entry ->
            setCategoryNameLocation(entry.key)
        }
    }


    private fun setPieChartRectDimensions(
        top: Float = 0f + pieMargin,
        bottom: Float = measuredHeight.toFloat() - pieMargin,
        left: Float = 0f + pieMargin,
        right: Float = measuredWidth - pieMargin
    ) {
        pieRect.top = top
        pieRect.bottom = bottom
        pieRect.left = left
        pieRect.right = right

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pieData != null) {
            // draw arcs with slices
            pieData?.pieModels?.let { data ->
                data.forEach { entry ->
                    canvas.drawArc(
                        pieRect,
                        entry.value.startAngle,
                        entry.value.sweepAngle,
                        true,
                        entry.value.paint
                    )

                    canvas.drawArc(
                        pieRect,
                        entry.value.startAngle,
                        entry.value.sweepAngle,
                        true,
                        separatorsPaint
                    )
                }
            }
            // draw inner circle
            canvas.drawCircle(
                pieRect.centerX(),
                pieRect.centerY(),
                innerRadius,
                innerPiePaint
            )

            // draw total amount
            pieData?.let { d ->
                val amountStr = "${d.totalAmount} ${d.currencyLabel}"
                val charCount = amountStr.length
                canvas.drawText(
                    amountStr,
                    pieRect.centerX(),
                    pieRect.centerY() + charCount * density,
                    totalAmountPaint
                )
            }
            // draw category names and lines
            pieData?.pieModels?.let { data ->
                data.forEach { entry ->
                    drawCategories(canvas, entry.value)
                }
            }
        }
    }

    private fun drawCategories(canvas: Canvas, pieModel: PieModel) {
        if (pieModel.categoryLocation.x < width / 2) {
            drawCategoryLine(canvas, pieModel, CategoryAlignment.LEFT)
            drawCategoryName(canvas, pieModel, CategoryAlignment.LEFT)
        } else {
            drawCategoryLine(canvas, pieModel, CategoryAlignment.RIGHT)
            drawCategoryName(canvas, pieModel, CategoryAlignment.RIGHT)
        }
        canvas.drawCircle(
            pieModel.categoryLocation.x,
            pieModel.categoryLocation.y,
            categoryNameCircleRadius,
            categoryCirclePaint
        )
    }

    private fun drawCategoryLine(
        canvas: Canvas,
        pieModel: PieModel,
        alignment: CategoryAlignment
    ) {
        val xOffset = if (alignment == CategoryAlignment.LEFT) {
            width / 4 * -1
        } else {
            width / 4
        }
        canvas.drawLine(
            pieModel.categoryLocation.x,
            pieModel.categoryLocation.y,
            pieModel.categoryLocation.x + xOffset,
            pieModel.categoryLocation.y,
            categoryLinePaint
        )
    }

    private fun drawCategoryName(
        canvas: Canvas,
        pieModel: PieModel,
        alignment: CategoryAlignment
    ) {
        val xOffset = if (alignment == CategoryAlignment.LEFT) {
            (width - pieMargin) / 4 * -1
        } else {
            (width - pieMargin) / 4
        }

        if (alignment == CategoryAlignment.LEFT) {
            categoryNamePaint.textAlign = Paint.Align.LEFT
        } else {
            categoryNamePaint.textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(
            pieModel.name,
            pieModel.categoryLocation.x + xOffset,
            pieModel.categoryLocation.y - 8,
            categoryNamePaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { e ->
            if (e.pointerCount > 1) {
                return super.onTouchEvent(event)
            }
            if (e.action == MotionEvent.ACTION_DOWN) {
                return true
            }
            if (e.action == MotionEvent.ACTION_UP) {
                if (checkUserTouchInCenterOfChart(e)) {
                    when (pieState) {
                        PieChartState.MINIMIZED -> {
                            expandPieChart()
                        }
                        PieChartState.EXPANDED -> {
                            collapsePieChart()
                        }
                    }
                } else {
                    processCategoryClick(e)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun processCategoryClick(e: MotionEvent) {
        pieData?.pieModels?.let { entry ->
            for ((name, model) in entry) {
                if (isClickOnModel(model, e)) {
                    categoryClickListener?.onCategoryClick(name)
                    break
                }
            }
        }
    }

    private fun isClickOnModel(model: PieModel, e: MotionEvent): Boolean {
        val x = e.x - pieRect.centerX()
        val y = e.y - pieRect.centerY()
        val eventAngle = getAngleOfEvent(x, y)
        return isWithinRadius(outerRadius, x, y) &&
                eventAngle > model.startAngle
                && eventAngle < model.sweepAngle + model.startAngle
    }

    /**
     * Returns the angle between 0 and the touch event with respect
     * to the center of the view.
     */
    private fun getAngleOfEvent(x: Float, y: Float): Float {
        val angle = (180 / Math.PI * atan2(y, x)).toFloat()
        return if (angle < 0) angle + 360 else angle
    }

    /**
     * Checks if the touch was within the pie chart radius. No need to check if the
     * touch happened exactly on the pie chart slice with category, because
     * all events inside the inner circle are handled by another touch handler ->
     * animation of the view.
     */
    private fun isWithinRadius(radius: Float, x: Float, y: Float): Boolean {
        return x * x + y * y <= radius * radius
    }

    private fun expandPieChart() {
        expandAnimator.setIntValues(height, (width / 1.3).toInt())
        categoryNameAnimator.setIntValues(0, 255)
        totalAmountAnimator.setIntValues(255, 0)
        animateExpansion.start()
    }

    private fun collapsePieChart() {
        initialHeight?.let {
            collapseAnimator.setIntValues(height, it)
            categoryNameAnimator.setIntValues(255, 0)
            totalAmountAnimator.setIntValues(0, 255)
            animateCollapse.start()
        }
    }

    /**
     * To check if user clicked inside the circle use the equation:
     * (x-center_x)^2 + (y - center_y)^2 < radius^2
     */
    private fun checkUserTouchInCenterOfChart(e: MotionEvent): Boolean {
        val touchX = e.x
        val touchY = e.y
        val centerX = pieRect.centerX()
        val centerY = pieRect.centerY()
        return (touchX - centerX).pow(2) + (touchY - centerY).pow(2) < innerRadius.pow(2)
    }

    private enum class CategoryAlignment {
        LEFT, RIGHT
    }

    private enum class PieChartState {
        MINIMIZED, EXPANDED
    }

    fun interface OnCategoryClickListener {
        fun onCategoryClick(name: String)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).also {
            it.pieData = pieData
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state)
                updateChart(state)
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    private fun updateChart(state: SavedState) {
        pieData = state.pieData
        requestLayout()
        invalidate()
    }

    private class SavedState : BaseSavedState {

        var pieData: PieData? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel?) : super(source) {
            source?.apply {
                pieData = source.readParcelable<PieData>(PieData::class.java.classLoader)
            }
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeParcelable(pieData, 0)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel?): SavedState =
                    SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> =
                    arrayOfNulls(size)
            }
        }

    }
}















