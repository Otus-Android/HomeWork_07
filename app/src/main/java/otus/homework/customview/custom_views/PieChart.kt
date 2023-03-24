package otus.homework.customview.custom_views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import otus.homework.customview.extensions.displayPercentage
import otus.homework.customview.extensions.readParcelList
import otus.homework.customview.model.Payload
import otus.homework.customview.model.PieChartModel
import kotlin.math.*

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val minChartSize by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimensionPixelSize(R.dimen.min_pie_chart_size)
    }

    private val textSpacing by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.text_spacing)
    }

    private val segmentTextSize by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.segment_text_size)
    }

    private val segmentTextBorderInset by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.segment_text_border_inset)
    }

    private val segmentTextBorderWidth by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.segment_text_border_width)
    }

    private val segmentTextBorderRadius by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.segment_text_border_radius)
    }

    private val segmentTextExpandedSize by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.segment_text_expanded_size)
    }

    private val totalTextSize by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.total_text_size)
    }

    private val segmentPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val segmentTextPaint by lazy(LazyThreadSafetyMode.NONE) {
        getDefaultTextPaint().apply {
            textSize = segmentTextSize
        }
    }

    private val totalTextPaint by lazy(LazyThreadSafetyMode.NONE) {
        getDefaultTextPaint().apply {
            textSize = totalTextSize
        }
    }

    private val segmentTextBorderPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = segmentTextBorderWidth
        }
    }

    private fun getDefaultTextPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
            textAlignment = TEXT_ALIGNMENT_CENTER
            typeface = setTextTypeFace()
        }
    }

    private val colors by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            getColor(R.color.first_segment_color) to getColor(R.color.first_segment_color_light),
            getColor(R.color.second_segment_color) to getColor(R.color.second_segment_color_light),
            getColor(R.color.third_segment_color) to getColor(R.color.third_segment_color_light),
            getColor(R.color.fourth_segment_color) to getColor(R.color.fourth_segment_color_light),
            getColor(R.color.fifth_segment_color) to getColor(R.color.fifth_segment_color_light),
            getColor(R.color.sixth_segment_color) to getColor(R.color.sixth_segment_color_light),
            getColor(R.color.seventh_segment_color) to getColor(R.color.seventh_segment_color_light),
            getColor(R.color.eighth_segment_color) to getColor(R.color.eighth_segment_color_light),
            getColor(R.color.ninth_segment_color) to getColor(R.color.ninth_segment_color_light),
            getColor(R.color.tenth_segment_color) to getColor(R.color.tenth_segment_color_light)
        )
    }

    private var innerStartPoint = PointF()
    private var outerStartPoint = PointF()

    private var innerExpandedRectF = RectF()
    private var viewRectF = RectF()

    private var innerRectF = RectF()
    private var outerRectF = RectF()

    private var expandedSize = 0

    private var chartData: List<PieChartModel> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val animatedIndexes = mutableSetOf<Int>()
    private var animatedSegments = mutableMapOf<Int, Float>()
    private var animatedSegmentTextSize = mutableMapOf<Int, Float>()

    private var animatedTotalTextPosition = -1f
    private var animatedTotalTextAlpha = -1

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                return handleTapUpEvent(event)
            }

            override fun onLongPress(event: MotionEvent) {
                handleLongPress(event)
            }
        }
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasuredDimension(widthMeasureSpec)
        val height = getMeasuredDimension(heightMeasureSpec)

        min(width, height).also { viewSize ->
            setMeasuredDimension(viewSize, viewSize)
        }
    }

    private fun getMeasuredDimension(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> max(minChartSize, specSize)
            else -> minChartSize
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())

        setExpandedSize()
        setOuterRect()
        setInnerRect()
        setInnerExpandedRect()
    }

    private fun setExpandedSize() {
        val expandedPartValue = EXPANDED_PARTIAL_VALUE.coerceIn(0.0, 0.15)
        expandedSize = (min(width, height) * expandedPartValue).roundToInt()
    }

    private fun setOuterRect() {
        val chartWidth = width.toFloat() - (2 * expandedSize)
        val chartHeight = height.toFloat() - (2 * expandedSize)

        outerRectF = RectF(
            expandedSize.toFloat(),
            expandedSize.toFloat(),
            chartWidth + expandedSize.toFloat(),
            chartHeight + expandedSize.toFloat()
        )
    }

    private fun setInnerRect() {
        val diffWidth = outerRectF.width() / OUTER_TO_INNER_RATIO
        val diffHeight = outerRectF.height() / OUTER_TO_INNER_RATIO

        innerRectF = RectF(
            outerRectF.centerX() - diffWidth,
            outerRectF.centerY() - diffHeight,
            outerRectF.centerX() + diffWidth,
            outerRectF.centerY() + diffHeight
        )
    }

    private fun setInnerExpandedRect() {
        innerExpandedRectF = RectF(
            innerRectF.left + expandedSize,
            innerRectF.top + expandedSize,
            innerRectF.right - expandedSize,
            innerRectF.bottom - expandedSize
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        setStartPointValues()

        chartData.forEachIndexed { index, it ->
            var expandedInnerRect = if (it.isSelected) innerExpandedRectF else innerRectF
            var expandedOuterRect = if (it.isSelected) viewRectF else outerRectF

            if (isAnimatedSegment(index)) {
                expandedInnerRect = getAnimatedInnerRect(index)
                expandedOuterRect = getAnimatedOuterRect(index)
            }

            val segmentPath = getSegmentPath(
                expandedInnerRect = expandedInnerRect,
                expandedOuterRect = expandedOuterRect,
                startAngle = it.startAngle,
                sweepAngle = it.sweepAngle
            )

            segmentPaint.setRadialGradient(
                expandedInnerRect = expandedInnerRect,
                expandedOuterRect = expandedOuterRect,
                colorPair = it.colorPair
            )

            canvas?.drawPath(segmentPath, segmentPaint)

            canvas?.drawSegmentText(
                expandedInnerRect = expandedInnerRect,
                expandedOuterRect = expandedOuterRect,
                percentage = it.percent,
                segmentMiddleAngle = it.getBisectorAngle(),
                isSelected = it.isSelected,
                textSize = it.getSegmentTextSize(index)
            )

            updateStartPointValues(
                expandedInnerRect = expandedInnerRect,
                expandedOuterRect = expandedOuterRect,
                addedAngle = it.endAngle
            )
        }

        if (needDrawTotalText()) {
            canvas?.drawTotalPercentText()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    fun setPayloadsData(payloads: List<Payload>) {
        val payloadsGroup = payloads.groupBy { it.category }
        val commonAmount = payloads.sumOf { it.amount }
        var startAngle = 0f
        val result = mutableListOf<PieChartModel>()

        payloadsGroup.forEach { (category, payloads) ->
            val categoryAmount = payloads.sumOf { it.amount }.toFloat()
            val percent = categoryAmount / commonAmount
            val sweepAngle = percent * DEGREES_360
            val endAngle = (startAngle + sweepAngle).coerceAtMost(DEGREES_360)

            result.add(
                PieChartModel(
                    category = category,
                    percent = percent,
                    colorPair = getColorPairByIndex(payloadsGroup.keys.indexOf(category)),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    endAngle = endAngle
                )
            )
            startAngle += sweepAngle
        }
        chartData = result.toList()
    }

    // ========================== DRAW_MAIN_ELEMENTS ==========================
    private fun Canvas.drawSegmentText(
        expandedInnerRect: RectF,
        expandedOuterRect: RectF,
        percentage: Float,
        segmentMiddleAngle: Float,
        isSelected: Boolean,
        textSize: Float
    ) {
        save()
        segmentTextPaint.textSize = textSize
        segmentTextPaint.typeface = setTextTypeFace(isBold = isSelected)

        val radius = (expandedOuterRect.width() / 2 + expandedInnerRect.width() / 2) / 2
        val projectionX = (radius * cos(Math.toRadians(segmentMiddleAngle.toDouble()))).toFloat()
        val projectionY = (radius * sin(Math.toRadians(segmentMiddleAngle.toDouble()))).toFloat()

        val segmentCenterX = outerRectF.centerX() + projectionX
        val segmentCenterY = outerRectF.centerY() + projectionY

        val text = percentage.displayPercentage(context)
        val textBounds = Rect()
        segmentTextPaint.getTextBounds(text, 0, text.length, textBounds)

        val textWidth = segmentTextPaint.measureText(text)
        val textHeight = textBounds.height().toFloat()

        val textX = segmentCenterX - textWidth / 2
        val textY = segmentCenterY + textHeight / 2

        rotate(
            segmentMiddleAngle.toRotatedAngle(),
            textX + textWidth / 2,
            textY - textHeight / 2
        )

        drawText(text, textX, textY, segmentTextPaint)
        drawSegmentTextBorder(
            textStartX = textX,
            textStartY = textY,
            textWidth = textWidth,
            textHeight = textHeight
        )
        restore()
    }

    private fun Canvas.drawSegmentTextBorder(
        textStartX: Float,
        textStartY: Float,
        textWidth: Float,
        textHeight: Float
    ) {
        val borderTextRect = RectF(
            textStartX - segmentTextBorderInset,
            textStartY - textHeight - segmentTextBorderInset,
            textStartX + textWidth + segmentTextBorderInset,
            textStartY + segmentTextBorderInset
        )

        drawRoundRect(
            borderTextRect,
            segmentTextBorderRadius,
            segmentTextBorderRadius,
            segmentTextBorderPaint
        )
    }

    private fun Canvas.drawTotalPercentText() {
        drawTotalTitle {
            drawTotalPercentValue(it)
        }
    }

    private fun Canvas.drawTotalTitle(callback: (Float) -> Unit) {
        val totalText = context.getString(R.string.total_x)
        val totalTextBounds = Rect()
        totalTextPaint.getTextBounds(totalText, 0, totalText.length, totalTextBounds)
        totalTextPaint.alpha = animatedTotalTextAlpha
        val totalTextWidth = totalTextPaint.measureText(totalText)

        val totalTextX = viewRectF.centerX() - totalTextWidth / 2
        val totalTextY = (viewRectF.centerY() - textSpacing) * animatedTotalTextPosition

        callback(totalTextY)
        drawText(totalText, totalTextX, totalTextY, totalTextPaint)
    }

    private fun Canvas.drawTotalPercentValue(positionY: Float) {
        val percentText = getSelectedCategoriesTotalPercent().displayPercentage(context)
        val percentTextBounds = Rect()
        totalTextPaint.getTextBounds(percentText, 0, percentText.length, percentTextBounds)

        val percentTextWidth = totalTextPaint.measureText(percentText)
        val percentTextHeight = percentTextBounds.height()

        val percentTextX = viewRectF.centerX() - percentTextWidth / 2
        val percentTextY = positionY + 2 * textSpacing + percentTextHeight
        drawText(percentText, percentTextX, percentTextY, totalTextPaint)
    }

    private fun getSegmentPath(
        expandedInnerRect: RectF,
        expandedOuterRect: RectF,
        startAngle: Float,
        sweepAngle: Float
    ): Path {
        return Path().apply {
            reset()
            arcTo(expandedOuterRect, startAngle, sweepAngle)
            arcTo(expandedInnerRect, startAngle + sweepAngle, -sweepAngle)
            close()
        }
    }

    private fun getAnimatedInnerRect(index: Int): RectF {
        val range = animatedSegments[index] ?: 0f
        return RectF(
            innerRectF.left + range,
            innerRectF.top + range,
            innerRectF.right - range,
            innerRectF.bottom - range
        )
    }

    private fun getAnimatedOuterRect(index: Int): RectF {
        val range = animatedSegments[index] ?: 0f
        return RectF(
            outerRectF.left - range,
            outerRectF.top - range,
            outerRectF.right + range,
            outerRectF.bottom + range
        )
    }

    private fun Paint.setRadialGradient(
        expandedInnerRect: RectF,
        expandedOuterRect: RectF,
        colorPair: Pair<Int, Int>
    ) {
        val gradientColors = intArrayOf(colorPair.second, colorPair.first)

        val radiusRelationship = expandedInnerRect.width() / expandedOuterRect.width()
        val stops = floatArrayOf(radiusRelationship, 1f)

        shader = RadialGradient(
            expandedOuterRect.centerX(),
            expandedOuterRect.centerY(),
            expandedOuterRect.width() / 2,
            gradientColors,
            stops,
            Shader.TileMode.CLAMP
        )
    }

    private fun PieChartModel.getSegmentTextSize(index: Int): Float {
        val staticTextSize = if (isSelected) segmentTextExpandedSize else segmentTextSize
        return if (isAnimatedSegmentTextSize(index))
            animatedSegmentTextSize[index] ?: staticTextSize
        else
            staticTextSize
    }

    private fun setStartPointValues() {
        innerStartPoint = PointF(innerRectF.right, innerRectF.centerY())
        outerStartPoint = PointF(outerRectF.right, outerRectF.centerY())
    }

    private fun updateStartPointValues(
        expandedInnerRect: RectF,
        expandedOuterRect: RectF,
        addedAngle: Float
    ) {
        val addedInnerX = calculateAddedValueX(expandedInnerRect.width(), addedAngle)
        val addedInnerY = calculateAddedValueY(expandedInnerRect.height(), addedAngle)
        val addedOuterX = calculateAddedValueX(expandedOuterRect.width(), addedAngle)
        val addedOuterY = calculateAddedValueY(expandedOuterRect.height(), addedAngle)

        val innerX = expandedInnerRect.centerX() + addedInnerX
        val innerY = expandedInnerRect.centerY() + addedInnerY

        val outerX = expandedOuterRect.centerX() + addedOuterX
        val outerY = expandedOuterRect.centerY() + addedOuterY

        innerStartPoint = PointF(innerX, innerY)
        outerStartPoint = PointF(outerX, outerY)
    }

    private fun calculateAddedValueX(rectWidth: Float, angle: Float): Float {
        val angleInRad = Math.toRadians(angle.toDouble())
        val absValue = abs((rectWidth / 2) * cos(angleInRad)).toFloat()
        return if (angle.isPositiveValueX()) absValue else -absValue
    }

    private fun calculateAddedValueY(rectWidth: Float, angle: Float): Float {
        val angleInRad = Math.toRadians(angle.toDouble())
        val absValue = abs((rectWidth / 2) * sin(angleInRad)).toFloat()
        return if (angle.isPositiveValueY()) absValue else -absValue
    }

    private fun getTouchAngleRelativeZeroDegrees(touchX: Float, touchY: Float): Float {
        val circleCenterX = viewRectF.centerX()
        val circleCenterY = viewRectF.centerY()

        val circleSystemTouchY = abs(touchY - viewRectF.centerY())
        val touchRadius = getTouchRadius(touchX, touchY)

        val touchAngle = Math.toDegrees(
            asin(circleSystemTouchY / touchRadius).toDouble()
        ).toFloat()

        return when {
            touchX > circleCenterX && touchY > circleCenterY -> touchAngle
            touchX < circleCenterX && touchY > circleCenterY -> DEGREES_180 - touchAngle
            touchX < circleCenterX && touchY < circleCenterY -> DEGREES_180 + touchAngle
            else -> DEGREES_360 - touchAngle
        }
    }

    private fun getTouchRadius(touchX: Float, touchY: Float): Float {
        val circleSystemTouchX = abs(touchX - viewRectF.centerX())
        val circleSystemTouchY = abs(touchY - viewRectF.centerY())
        return sqrt(circleSystemTouchX.pow(2) + circleSystemTouchY.pow(2))
    }

    private fun handleTapUpEvent(event: MotionEvent): Boolean {
        findSegmentByTouchEvent(
            event = event
        ) {
            val index = chartData.indexOf(it)
            if (animatedIndexes.contains(index))
                return@findSegmentByTouchEvent

            doClickAction(
                foundedSegment = it,
                index = index
            )
        }
        return true
    }

    private fun handleLongPress(event: MotionEvent) {
        findSegmentByTouchEvent(
            event = event
        ) {
            Toast.makeText(context, it.category, Toast.LENGTH_LONG).show()
        }
    }

    private fun findSegmentByTouchEvent(
        event: MotionEvent,
        callback: (PieChartModel) -> Unit
    ) {
        val touchX = event.x
        val touchY = event.y

        if (touchPointInsideSegments(touchX, touchY)) {
            val touchDegrees = getTouchAngleRelativeZeroDegrees(touchX, touchY)
            val foundedSegment = chartData.firstOrNull {
                touchDegrees in it.startAngle..it.endAngle
            }
            if (foundedSegment != null) {
                callback(foundedSegment)
            }
        }
    }

    private fun doClickAction(foundedSegment: PieChartModel, index: Int) {
        val updatedElement = foundedSegment.copy(isSelected = !foundedSegment.isSelected)

        val mChartData = chartData.toMutableList()
        mChartData[index] = updatedElement

        animatedIndexes.add(index)

        if (needStartAppearanceTotalTextAnimation(mChartData)) {
            getIncreaseTotalTextAnimatorSet().start()
        }
        if (needStartDisappearanceTotalTextAnimation(mChartData)) {
            getDecreaseTotalTextAnimatorSet().start()
        }
        chartData = mChartData.toList()

        if (chartData[index].isSelected)
            getIncreaseSegmentAnimatorSet(index).start()
        else
            getDecreaseSegmentAnimatorSet(index).start()
    }


    // ==================== COMMON_CONDITIONS_METHODS =========================
    private fun isAnimatedSegment(index: Int): Boolean {
        return animatedSegments.contains(index) && animatedIndexes.contains(index)
    }

    private fun isAnimatedSegmentTextSize(index: Int): Boolean {
        return isAnimatedSegment(index) && animatedSegmentTextSize.contains(index)
    }

    private fun needDrawTotalText(): Boolean {
        return chartData.any { it.isSelected } ||
                animatedTotalTextPosition > -1f ||
                animatedTotalTextAlpha > -1
    }

    private fun needStartAppearanceTotalTextAnimation(
        updatedList: List<PieChartModel>
    ): Boolean {
        return chartData.count { it.isSelected } == 0 && updatedList.count { it.isSelected } == 1
    }

    private fun needStartDisappearanceTotalTextAnimation(
        updatedList: List<PieChartModel>
    ): Boolean {
        return chartData.count { it.isSelected } == 1 && updatedList.count { it.isSelected } == 0
    }

    private fun touchPointInsideSegments(touchX: Float, touchY: Float): Boolean {
        val innerRadius = innerRectF.width() / 2
        val outerRadius = outerRectF.width() / 2
        val touchRadius = getTouchRadius(touchX, touchY)
        return touchRadius in innerRadius..outerRadius
    }

    // ============================ ANIMATION_BLOCK ===========================
    /**
     * Animations for increase / decrease segments size after clicking on them
     */
    private fun getIncreaseSegmentAnimatorSet(index: Int): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                getIncreaseSegmentSizeAnimator(index),
                getIncreaseSegmentTextSizeAnimator(index)
            )
        }
    }

    private fun getDecreaseSegmentAnimatorSet(index: Int): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                getDecreaseSegmentSizeAnimator(index),
                getDecreaseSegmentTextSizeAnimator(index)
            )
        }
    }

    private fun getIncreaseSegmentSizeAnimator(index: Int): ValueAnimator {
        return ValueAnimator.ofFloat(0f, expandedSize.toFloat())
            .apply {
                duration = ANIM_LENGTH
                interpolator = BounceInterpolator()
                addUpdateListener {
                    animatedSegments[index] = it.animatedValue as Float
                    invalidate()
                }
                addListener(
                    onEnd = {
                        animatedIndexes.remove(index)
                        animatedSegments.remove(index)
                    }
                )
            }
    }

    private fun getDecreaseSegmentSizeAnimator(index: Int): ValueAnimator {
        return ValueAnimator.ofFloat(expandedSize.toFloat(), 0f)
            .apply {
                duration = ANIM_LENGTH
                interpolator = BounceInterpolator()
                addUpdateListener {
                    animatedSegments[index] = it.animatedValue as Float
                    invalidate()
                }
                addListener(
                    onEnd = {
                        animatedIndexes.remove(index)
                        animatedSegments.remove(index)
                    }
                )
            }
    }

    private fun getIncreaseSegmentTextSizeAnimator(index: Int): ValueAnimator {
        return ValueAnimator.ofFloat(segmentTextSize, segmentTextExpandedSize)
            .apply {
                duration = ANIM_LENGTH
                interpolator = BounceInterpolator()
                addUpdateListener {
                    animatedSegmentTextSize[index] = it.animatedValue as Float
                }
                addListener(
                    onEnd = {
                        animatedSegmentTextSize.remove(index)
                    }
                )
            }
    }

    private fun getDecreaseSegmentTextSizeAnimator(index: Int): ValueAnimator {
        return ValueAnimator.ofFloat(segmentTextExpandedSize, segmentTextSize)
            .apply {
                duration = ANIM_LENGTH
                interpolator = BounceInterpolator()
                addUpdateListener {
                    animatedSegmentTextSize[index] = it.animatedValue as Float
                }
                addListener(
                    onEnd = {
                        animatedSegmentTextSize.remove(index)
                    }
                )
            }
    }

    /**
     * Animations for appearance / disappearance total percent text
     */
    private fun getIncreaseTotalTextAnimatorSet(): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                getAppearanceTotalTextAnimator(),
                getIncreaseTotalTextAlphaAnimator()
            )
        }
    }

    private fun getDecreaseTotalTextAnimatorSet(): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                getDisappearanceTotalTextAnimator(),
                getDecreaseTotalTextAlphaAnimator()
            )
        }
    }

    private fun getAppearanceTotalTextAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(0.7f, 1f)
            .apply {
                duration = SHORT_ANIM_LENGTH
                interpolator = BounceInterpolator()
                addUpdateListener {
                    animatedTotalTextPosition = it.animatedValue as Float
                    invalidate()
                }
            }
    }

    private fun getDisappearanceTotalTextAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 1.3f)
            .apply {
                duration = SHORT_ANIM_LENGTH
                addUpdateListener {
                    animatedTotalTextPosition = it.animatedValue as Float
                    invalidate()
                }
            }
    }

    private fun getIncreaseTotalTextAlphaAnimator(): ValueAnimator {
        return ValueAnimator.ofInt(0, 255)
            .apply {
                duration = SHORT_ANIM_LENGTH
                addUpdateListener {
                    animatedTotalTextAlpha = it.animatedValue as Int
                    invalidate()
                }
            }
    }

    private fun getDecreaseTotalTextAlphaAnimator(): ValueAnimator {
        return ValueAnimator.ofInt(255, 0)
            .apply {
                duration = SHORT_ANIM_LENGTH
                addUpdateListener {
                    animatedTotalTextAlpha = it.animatedValue as Int
                    invalidate()
                }
            }
    }

    // ========================== COMMON_METHODS ==============================
    private fun getSelectedCategoriesTotalPercent(): Float {
        return chartData.filter { it.isSelected }.map { it.percent }.sum()
    }

    private fun getColorPairByIndex(index: Int): Pair<Int, Int> {
        return colors.getOrNull(index) ?: colors.first()
    }

    private fun Float.isPositiveValueX(): Boolean {
        return this in DEGREES_0..DEGREES_90 || this in DEGREES_270..DEGREES_360
    }

    private fun Float.isPositiveValueY(): Boolean {
        return this in DEGREES_0..DEGREES_180
    }

    private fun Float.toRotatedAngle(): Float {
        return if (this in DEGREES_90..DEGREES_270) {
            this - DEGREES_180
        } else {
            this
        }
    }

    private fun setTextTypeFace(isBold: Boolean = false): Typeface {
        val textStyle = if (isBold) Typeface.BOLD else Typeface.NORMAL
        return Typeface.create(Typeface.SERIF, textStyle)
    }

    private fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    // ========================== SAVED_RESTORE_STATE ==============================
    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                chartData = state.segments
                animatedTotalTextPosition = state.totalTextValuePosition
                animatedTotalTextAlpha = state.totalTextValueAlpha
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    private inner class SavedState : BaseSavedState {
        var segments = emptyList<PieChartModel>()
        var totalTextValuePosition = -1f
        var totalTextValueAlpha = -1

        constructor(source: Parcelable?) : super(source) {
            segments = chartData
            totalTextValuePosition = animatedTotalTextPosition
            totalTextValueAlpha = animatedTotalTextAlpha
        }

        constructor(source: Parcel?) : super(source) {
            source?.readParcelList(segments)
            totalTextValuePosition = source?.readFloat() ?: -1f
            totalTextValueAlpha = source?.readInt() ?: -1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(segments)
            out.writeFloat(totalTextValuePosition)
            out.writeInt(totalTextValueAlpha)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(source: Parcel?): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val EXPANDED_PARTIAL_VALUE = 0.05
        private const val OUTER_TO_INNER_RATIO = 3.5f
        private const val DEGREES_0 = 0f
        private const val DEGREES_90 = 90f
        private const val DEGREES_180 = 180f
        private const val DEGREES_270 = 270f
        private const val DEGREES_360 = 360f

        private const val ANIM_LENGTH = 1500L
        private const val SHORT_ANIM_LENGTH = 800L
    }

}