package otus.homework.customview.ui.pie_chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.withTranslation
import kotlin.math.*

class PieChart(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var viewItems: List<ChartItem> = emptyList()
    private var selectedItem: ChartItem? = null
        set(value) {
            selectionListener(items.find { it.id == value?.id })
            if (value?.id == field?.id) return
            field?.isSelected = false
            field = value
            field?.isSelected = true
            invalidate()
        }
    private var focusedItem: ChartItem? = null
        set(value) {
            if (value?.id == field?.id) return
            field?.isFocused = false
            field = value
            field?.isFocused = true
            invalidate()
        }

    // TODO: Obtain all as styleable attributes
    // TODO: Add labels customization

    private val defaultViewSize = 500
    private val minViewSize = 250
    private val minClickableArea = 80f

    private val labelSize = 36f
    private val labelOffset = 40f
    private val labelPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = labelSize
    }

    private val detailsMainTextSize = 60f
    private val detailsSecondaryTextSize = 40f

    private var amountPosition: PointF = PointF()
    private val amountSize = detailsSecondaryTextSize
    private val amountMargin = 30f
    private val amountPaint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        textSize = amountSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }

    private var percentPosition: PointF = PointF()
    private val percentSize = detailsMainTextSize
    private val percentPaint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        textSize = percentSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }

    private val segmentCap = Paint.Cap.BUTT
    private val segmentPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = unselectedSegmentWidth
        strokeCap = segmentCap
    }

    // TODO: optimize and replace `calculateDimensions` with more narrow recalculation or redrawing

    /**
     * If `false` - increase size of segment circle to fill entire view if labels are hidden,
     * preserve size of segments otherwise.
     */
    var preserveCircleSize = true
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var segmentStartAngle = MIN_ANGLE
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var unselectedSegmentWidth = 30f
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var selectedSegmentWidth = 60f
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var sort: Sort? = null
        set(value) {
            field = value
            viewItems = viewItems.sortedBy(value, order)
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var order: Order = Order.ASCENDING
        set(value) {
            field = value
            viewItems = viewItems.sortedBy(sort, value)
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var showLabels = true // TODO: add animation
        set(value) {
            field = value
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var showDetailsPercent = true // TODO: add animation
        set(value) {
            field = value
            amountPaint.textSize = if (value) detailsSecondaryTextSize else detailsMainTextSize
            calculateDimensions(measuredWidth, measuredHeight)
        }

    var showDetailsAmount = true // TODO: add animation
        set(value) {
            field = value
            amountPaint.textSize = if (showDetailsPercent) detailsSecondaryTextSize else detailsMainTextSize
            calculateDimensions(measuredWidth, measuredHeight)
        }

    /**
     * Listener of clicks on already selected item (second click on item)
     * or `null` if click was performed outside of items
     */
    var doubleClickListener: PieChart.(PieChartItem?) -> Unit = {}

    /**
     * Listener of selected item (first click on item)
     * or `null` if click was performed outside of items
     */
    var selectionListener: PieChart.(PieChartItem?) -> Unit = {}

    var items: List<PieChartItem> = emptyList()
        private set

    /**
     * Show items as colored segments with labels in Pie Chart.
     *
     * @param fromItem show items with animation as subcategories of [fromItem] // TODO: not implemented
     */
    fun display(items: List<PieChartItem>, fromItem: PieChartItem? = null) {
        focusedItem = null
        selectedItem = null

        this.items = items
        this.viewItems = items
            .mapToInternalItems()
            .sortedBy(sort, order)

        calculateDimensions(measuredWidth, measuredHeight)
    }

    fun select(item: PieChartItem) {
        viewItems.find { it.id == item.id }
            ?.let {
                selectedItem = it
            }
            ?: Log.w(TAG, "There is no such item = $item")
    }

    fun clearSelection() {
        selectedItem = null
    }

    private fun List<PieChartItem>.mapToInternalItems(): List<ChartItem> {
        val totalAmount = fold(0) { acc, item -> acc + item.amount }.toFloat()
        var startAngle = segmentStartAngle

        return map { item ->
            item.map(totalAmount, startAngle).also {
                startAngle += it.segmentSweepAngle
            }
        }
    }

    private fun PieChartItem.map(totalAmount: Float, startAngle: Float): ChartItem {
        val cache = viewItems.find { id == it.id } // TODO: very slow and ignores item updates
        if (cache != null) return cache

        val amountPercent = amount / totalAmount
        val sweepAngle = MAX_ANGLE.toFloat() * amountPercent
        return ChartItem(
            id = id,
            label = label,
            amount = amount,
            amountPercent = amountPercent,
            segmentRectF = RectF(),
            segmentStartAngle = startAngle,
            segmentSweepAngle = sweepAngle,
            segmentColor = segmentColor,
        )
    }

    private fun List<ChartItem>.sortedBy(sort: Sort?, order: Order): List<ChartItem> = when (sort) {
        null -> this
        Sort.BY_ID -> when (order) {
            Order.ASCENDING -> sortedBy { it.id }
            Order.DESCENDING -> sortedByDescending { it.id }
        }
        Sort.BY_LABEL -> when (order) {
            Order.ASCENDING -> sortedBy { it.label }
            Order.DESCENDING -> sortedByDescending { it.label }
        }
        Sort.BY_AMOUNT -> when (order) {
            Order.ASCENDING -> sortedBy { it.amount }
            Order.DESCENDING -> sortedByDescending { it.amount }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = max(defaultViewSize, suggestedMinimumWidth + paddingLeft + paddingRight)
        val desiredHeight = max(defaultViewSize, suggestedMinimumHeight + paddingTop + paddingBottom)
        val size = min(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
        calculateDimensions(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateDimensions(w, h)
    }

    // TODO: cache calculations
    private fun calculateDimensions(width: Int, height: Int) {
        val contentSize = calculateContentSize(width, height)
        val segmentsRadius = calculateSegmentsRadius(contentSize)

        val left = 0.5f * width - segmentsRadius
        val right = 0.5f * width + segmentsRadius
        val bottom = 0.5f * height + segmentsRadius
        val top = 0.5f * height - segmentsRadius
        val segmentsRectF = RectF(left, top, right, bottom)
        val centerX = segmentsRectF.centerX()
        val centerY = segmentsRectF.centerX()

        calculateDetailsDimensions(centerX, centerY)

        val labelDistanceFromCenter = segmentsRadius + unselectedSegmentWidth * 0.5f + labelOffset
        calculateItemDimensions(segmentsRectF, centerX, centerY, labelDistanceFromCenter, contentSize)

        invalidate()
    }

    private fun calculateContentSize(width: Int, height: Int): Int {
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        return max(minViewSize, min(contentWidth, contentHeight))
    }

    private fun calculateSegmentsRadius(viewSize: Int): Float {
        val marginMultiplier = if (showLabels || preserveCircleSize) {
            CIRCLE_MARGIN_WITH_LABELS_PERCENT
        } else {
            CIRCLE_MARGIN_PERCENT
        }
        val circleMargin = viewSize * marginMultiplier + unselectedSegmentWidth * 0.5f
        return 0.5f * viewSize - circleMargin
    }

    private fun calculateDetailsDimensions(centerX: Float, centerY: Float) {
        when {
            showDetailsPercent && showDetailsAmount -> {
                percentPosition = PointF(centerX, centerY)
                amountPosition = PointF(centerX, centerY + amountPaint.textSize + amountMargin)
            }
            showDetailsPercent -> {
                percentPosition = PointF(centerX, centerY + 0.5f * percentPaint.textSize)
            }
            showDetailsAmount -> {
                amountPosition = PointF(centerX, centerY + 0.5f * percentPaint.textSize)
            }
        }
    }

    private fun calculateItemDimensions(
        segmentsRectF: RectF,
        centerX: Float,
        centerY: Float,
        labelDistanceFromCenter: Float,
        smallestSize: Int
    ) {
        viewItems.fold(segmentStartAngle) { startAngle, item ->
            with(item) {
                segmentStartAngle = startAngle
                segmentRectF = segmentsRectF
                segmentWidth = if (selectedItem?.id == id) selectedSegmentWidth else unselectedSegmentWidth
                if (showLabels) calculateLabelPosition(centerX, centerY, labelDistanceFromCenter, smallestSize)
                startAngle + segmentSweepAngle
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        viewItems.forEach {
            it.drawSegment(canvas)
            if (showLabels) it.drawLabel(canvas)
        }
        (focusedItem ?: selectedItem)?.drawDetails(canvas)
    }

    private fun ChartItem.drawDetails(canvas: Canvas) = with(canvas) {
        if (showDetailsPercent) drawText(percentString, percentPosition.x, percentPosition.y, percentPaint)
        if (showDetailsAmount) drawText(amountString, amountPosition.x, amountPosition.y, amountPaint)
    }

    private var touchDownItem: ChartItem? = null

    override fun onTouchEvent(event: MotionEvent): Boolean = with(event) {
        val item = findItemByPoint(x, y)
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> onTouchDown(item)
            MotionEvent.ACTION_MOVE -> onTouchMove(item)
            MotionEvent.ACTION_UP -> onTouchUp(item)
            MotionEvent.ACTION_CANCEL -> return false
        }
        return true
    }

    private fun onTouchDown(touchedItem: ChartItem?) {
        touchDownItem = touchedItem
        focusedItem = touchedItem
    }

    private fun onTouchMove(touchedItem: ChartItem?) {
        focusedItem = touchedItem
    }

    private fun onTouchUp(touchedItem: ChartItem?) {
        if (touchedItem == null && touchDownItem == null) doubleClickListener(null) // inform about click outside of items
        if (touchedItem != null
            && touchedItem.id == touchDownItem?.id // click only performed if DOWN was on the same item
            && touchedItem.id == selectedItem?.id // click only performed on already selected item (aka double click)
        ) {
            doubleClickListener(
                with(touchedItem) {
                    PieChartItem(id, label, amount, segmentColor)
                }
            )
        }
        if (touchedItem != null) selectedItem = touchedItem
        focusedItem = null
        performClick()
    }

    private fun findItemByPoint(x: Float, y: Float): ChartItem? {
        val segmentsArea = viewItems.firstOrNull()?.segmentRectF ?: return null
        val segmentsRadius = 0.5f * segmentsArea.width()
        val segmentHalfWidth = 0.5f * max(minClickableArea, unselectedSegmentWidth)
        val segmentRangeFromCenter = (segmentsRadius - segmentHalfWidth)..(segmentsRadius + segmentHalfWidth)

        val xFromCenter = x - 0.5f * measuredWidth
        val yFromCenter = y - 0.5f * measuredHeight
        val pointDistanceFromCenter = sqrt(xFromCenter * xFromCenter + yFromCenter * yFromCenter)
        if (pointDistanceFromCenter !in segmentRangeFromCenter) return null

        val pointAngle = calculateAngle(xFromCenter, yFromCenter)
        return viewItems.find {
            val endSegmentAngle = it.segmentStartAngle + it.segmentSweepAngle
            if (endSegmentAngle > MAX_ANGLE) {
                pointAngle in (it.segmentStartAngle..MAX_ANGLE.toFloat())
                        || pointAngle in 0f..(endSegmentAngle % MAX_ANGLE.toFloat())
            } else {
                pointAngle in it.segmentStartAngle..endSegmentAngle
            }
        }
    }

    private fun calculateAngle(x: Float, y: Float): Float {
        fun angleFromTangents() = Math.toDegrees(atan(y.toDouble() / x.toDouble())).toFloat()
        return when {
            x > 0 && y < 0 -> angleFromTangents() + 360f // right top quarter
            x > 0 -> angleFromTangents() // right bottom quarter
            x < 0 -> angleFromTangents() + 180f // left top and bottom quarters
            else -> if (y < 0) 270f else 90f // top and bottom points
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            selectedItemId = selectedItem?.id ?: NON_SELECTED_ITEM_ID
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            selectedItem = viewItems.find { it.id == state.selectedItemId }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {

        var selectedItemId: Int = NON_SELECTED_ITEM_ID

        constructor(parcelable: Parcelable?) : super(parcelable)

        constructor(source: Parcel?) : super(source) {
            source?.run {
                selectedItemId = readInt()
            }
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.apply {
                writeInt(selectedItemId)
            }
        }

        companion object {

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel?): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

    }

    /**
     * View data and methods related to specific item
     */
    inner class ChartItem(
        val id: Int,
        val label: String,
        val amount: Int,
        @FloatRange(from = 0.0, to = 1.0) val amountPercent: Float,
        var segmentRectF: RectF,
        var segmentWidth: Float = unselectedSegmentWidth,
        var segmentStartAngle: Float,
        var segmentSweepAngle: Float,
        @ColorInt var segmentColor: Int,
        var labelPosition: PointF = PointF(),
    ) {

        val amountString: String = amount.toString()
        val percentString = "%.2f%%".format(amountPercent * 100)
        private var labelLayout: StaticLayout = StaticLayout.Builder
            .obtain(label, 0, label.lastIndex, labelPaint, 0)
            .build()

        var isSelected: Boolean = false
            set(value) {
                field = value
                animateSelection(isSelected = value || isFocused)
            }

        var isFocused: Boolean = false
            set(value) {
                field = value
                animateSelection(isSelected = value || isSelected)
            }

        private fun animateSelection(isSelected: Boolean) {
            val targetSegmentWidth = if (isSelected) selectedSegmentWidth else unselectedSegmentWidth
            ValueAnimator.ofFloat(segmentWidth, targetSegmentWidth)
                .apply {
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { width ->
                        segmentWidth = width.animatedValue as Float
                        invalidate()
                    }
                }
                .start()
        }

        // TODO: fix non-equal distance for different angles
        fun calculateLabelPosition(
            centerX: Float,
            centerY: Float,
            labelDistanceFromCenter: Float,
            contentWidth: Int
        ) {
            val segmentCenterAngle = segmentStartAngle + segmentSweepAngle * 0.5
            val centerAngleRadian = Math.toRadians(segmentCenterAngle)
            val sin = sin(centerAngleRadian).toFloat()
            val cos = cos(centerAngleRadian).toFloat()

            val yOffsetFromCenter = labelDistanceFromCenter * sin
            val xOffsetFromCenter = labelDistanceFromCenter * cos

            val labelMaxWidth = contentWidth * 0.5f - abs(xOffsetFromCenter)
            labelLayout = StaticLayout.Builder
                .obtain(label, 0, label.length, labelPaint, labelMaxWidth.toInt())
                .build()
            // extra offset shift center of label to center of segment
            val yExtraOffset = labelLayout.height * (0.5f * (sin - 1))
            val xExtraOffset = labelLayout.width * (0.5f * (cos - 1))

            labelPosition.x = centerX + xOffsetFromCenter + xExtraOffset
            labelPosition.y = centerY + yOffsetFromCenter + yExtraOffset
        }

        fun drawSegment(canvas: Canvas) {
            segmentPaint.apply {
                color = segmentColor
                strokeWidth = segmentWidth
            }
            canvas.drawArc(segmentRectF, segmentStartAngle, segmentSweepAngle, false, segmentPaint)
        }

        fun drawLabel(canvas: Canvas) {
            canvas.withTranslation(labelPosition.x, labelPosition.y) {
                labelLayout.draw(canvas)
            }
        }

    }

    enum class Order {
        ASCENDING,
        DESCENDING,
    }

    enum class Sort {
        BY_ID,
        BY_LABEL,
        BY_AMOUNT,
    }

    companion object {
        private const val TAG = "PieChart"
        private const val NON_SELECTED_ITEM_ID = -1
        private const val MIN_ANGLE = 0.0f
        private const val MAX_ANGLE = 360.0
        private const val CIRCLE_MARGIN_PERCENT = 0.0f
        private const val CIRCLE_MARGIN_WITH_LABELS_PERCENT = 0.25f
    }

}
