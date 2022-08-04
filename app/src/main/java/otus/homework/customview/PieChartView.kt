package otus.homework.customview

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.*
import kotlin.random.Random

data class ChartData(
    val amount: Int,
    val name: String,
    val id: Int,
    val category: String,
    val time: Long
)

open class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    // temp variables
    private val tempPath = Path()
    private val tempRectF = RectF()
    private val tempRect = Rect()
    private val colorValuesArr = FloatArray(3)

    // ui related constans
    private var constGapWidth = (8 * resources.displayMetrics.density)

    // displayed data
    private var data: List<ChartData> = listOf()

    // ui settable variables
    private var offsetAngle = 0f
    private var groupByCategories: Boolean = true
    private var seed = System.currentTimeMillis()
    private var colorStack = updateColorStack()

    // listeners
    var sectorClickListener: SectorClickListener? = null

    // cache drawing related variables
    private var cacheDisplayHeight = context.resources.displayMetrics.heightPixels
    private var cacheDisplayWidth = context.resources.displayMetrics.widthPixels
    private var cacheGraphCenterY: Int = 0
    private var cacheGraphCenterX: Int = 0

    private val cacheCategories: MutableList<String> = mutableListOf()
    private val cacheDataAmounts: MutableList<Int> = mutableListOf()
    private var cacheDataAmountSum: Int = 1
    private val cacheCategoriesAmounts: MutableList<Int> = mutableListOf()
    private var cacheCategoriesAmountsSum: Int = 1
    private var cacheGraphWidth = 0
    private var cacheGraphHeight = 0
    private val cacheGraphDrawArea = RectF()
    private var cachedRadius: Float = 1f
    private var cacheGapAngleBetweenArcs: Float = 1f
    private var currentlyDisplayedText: String? = null
    private var textColor: Int = 0x7FFFFFFF
    private var hasAnimatedForTheFirstTime = false
    private val paint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.FILL
            textSize = 17 * resources.displayMetrics.scaledDensity
        }

    init {
        isClickable = true
        setData(data)
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.colorOnSurface))
        textColor = typedArray.getInteger(0, 0x7fffffff)
    }

    private fun updateColorStack(): ColorStack {
        colorStack =
            ColorStack(colors.mapIndexed { index, i -> Pair(index, i) }.shuffled(Random(seed)))
        return colorStack
    }

    fun setData(data: List<ChartData>) {
        this.data = data
        cacheDataAmountSum = data.sumBy {
            it.amount
        }
        cacheCategoriesAmounts.clear()
        val categoriesSortedDesc = data.groupBy {
            it.category
        }.map { (string, list) -> Pair(string, list.sumBy { it.amount }) }
            .sortedByDescending { item -> item.second }
        cacheCategories.clear()
        cacheCategories.addAll(categoriesSortedDesc.map { it.first })
        cacheCategoriesAmounts.addAll(
            categoriesSortedDesc.runningFold(0) { left, right ->
                left + right.second
            }
        )
        cacheCategoriesAmountsSum = categoriesSortedDesc.sumBy { it.second }
        cacheDataAmounts.clear()
        cacheDataAmounts.addAll(
            data.runningFold(0) { left, right ->
                left + right.amount
            }
        )
        cacheDataAmounts.clear()
    }

    fun setGroupByCategories(boolean: Boolean) {
        this.groupByCategories = boolean
        invalidate()
    }

    fun getGroupByCategories(): Boolean {
        return this.groupByCategories
    }

    private fun convertRadialToA(out: RectF, ro: Float, phi: Float) {
        val x = ro * cos(phi)
        val y = ro * sin(phi)
        out.setXY(x, y)
    }

    private fun RectF.setXY(x: Float, y: Float) {
        set(x, y, 0f, 0f)
    }

    private fun RectF.x() = left
    private fun RectF.y() = top

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        val heightMode = MeasureSpec.getMode(heightMeasureSpec);
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minDisplaySize = cacheDisplayWidth.coerceAtMost(cacheDisplayHeight)
        val resultWidthSize = when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                widthSize
            }
            else -> minDisplaySize
        }
        val resultHeightSize = when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                heightSize
            }
            else -> minDisplaySize
        }
        setMeasuredDimension(resultWidthSize, resultHeightSize)
    }

    private fun convertDecartToRadial(out: RectF, x: Float, y: Float) {
        val angle = when {
            x > 0f && y >= 0f -> atan(y / x) * 180 / 3.14f
            x > 0f && y < 0f -> atan(y / x) * 180 / 3.14f + 360f
            x < 0f -> atan(y / x) * 180 / 3.14f + 180f
            x == 0f && y > 0f -> 90f
            x == 0f && y < 0f -> 270f
            else -> 0f
        }
        val distance = sqrt(x.pow(2) + y.pow(2))
        out.setXY(distance, angle)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = this.width
        val height = this.height
        cacheGraphWidth = this.width - (paddingStart) - paddingEnd
        cacheGraphHeight = height - paddingTop - paddingBottom
        cacheGraphCenterX = paddingStart + (this.width / 2)
        cacheGraphCenterY = paddingTop + (this.height / 2)
        val min = cacheGraphWidth.coerceAtMost(cacheGraphHeight)
        val radius = min / 2f
        val graphLeft = (this.width / 2f) - radius
        val graphRight = graphLeft + 2 * radius
        val graphTop = (height / 2f) - radius
        val graphBottom = graphTop + 2 * radius
        cacheGraphDrawArea.set(graphLeft, graphTop, graphRight, graphBottom)
        cachedRadius = radius
        cacheGapAngleBetweenArcs = constGapWidth / radius * 180f / 3.14f
    }

    private fun getTotalDataAmount(): Int {
        return if (groupByCategories) cacheCategoriesAmountsSum else cacheDataAmountSum
    }

    private fun getElementsAmount(): Int {
        return if (groupByCategories) cacheCategories.size else data.size
    }

    private fun getAngleGap(): Float {
        return cacheGapAngleBetweenArcs
    }

    private fun getStartingAngleForIElement(
        index: Int,
        scalingFactor: Float,
        gapAngleSize: Float,
    ): Float {
        val amount = if (groupByCategories) {
            cacheCategoriesAmounts[index]
        } else {
            cacheDataAmounts[index]
        }
        return amount * scalingFactor + index * gapAngleSize + offsetAngle
    }

    private fun getSweepAngle(index: Int, scalingFactor: Float): Float {
        val amount = if (groupByCategories) {
            cacheCategoriesAmounts[index]
        } else {
            cacheDataAmounts[index]
        }
        val next = if (groupByCategories) {
            cacheCategoriesAmounts[index + 1]
        } else {
            cacheDataAmounts[index + 1]
        }
        return (next - amount.toFloat()) * scalingFactor
    }

    private fun Canvas.drawArc(
        dataIndex: Int,
        scalingFactor: Float,
        circleRect: RectF,
        gapSize: Float,
        paint: Paint,
    ) {
        val startingAngle = getStartingAngleForIElement(dataIndex, scalingFactor, gapSize)
        val sweepAngle = getSweepAngle(dataIndex, scalingFactor)

        drawArc(circleRect, startingAngle, sweepAngle, true, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!hasAnimatedForTheFirstTime) {
            duration = 1000L
            hasAnimatedForTheFirstTime = true
            setOffset(270f)
            duration = 100L
        }
        tempPath.reset()
        colorStack.reset()
        val radius = cachedRadius
        val totalAmountToDisplay = getTotalDataAmount()
        val gapAngle = getAngleGap()
        val scalingFactor = (360 - getElementsAmount() * gapAngle) / totalAmountToDisplay
        val innerRadius = (1 - constTorWidthCoef) * radius

        canvas.save()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipOutPath(tempPath.apply {
                addCircle(this@PieChartView.width / 2f,
                    height / 2f,
                    innerRadius,
                    Path.Direction.CCW)
            })
        } else {
            canvas.clipPath(tempPath.apply {
                addCircle(this@PieChartView.width / 2f,
                    height / 2f,
                    innerRadius,
                    Path.Direction.CCW)
            }, Region.Op.DIFFERENCE)
        }
        var i = 0
        while (i < getElementsAmount()) {
            canvas.drawArc(
                i,
                scalingFactor,
                cacheGraphDrawArea,
                gapAngle,
                paint.apply {
                    color = colorStack.nextColor()
                })
            i++
        }
        canvas.restore()

        val text = currentlyDisplayedText
        if (text != null) {
            paint.getTextBounds(text, 0, text.length, tempRect)

            canvas.drawText(text,
                this.width / 2f - (tempRect.width() / 2f),
                this.height / 2f + (tempRect.height() / 2f), paint.apply {
                    color = textColor
                })
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()
        val savedState = SavedState(parcelable)
        savedState.apply {
            offsetAngle = this@PieChartView.offsetAngle
            seed = this@PieChartView.seed
            groupByCategories = this@PieChartView.groupByCategories
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        savedState.apply {
            this@PieChartView.offsetAngle = offsetAngle
            this@PieChartView.seed = seed
            this@PieChartView.groupByCategories = groupByCategories
        }
        updateColorStack()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_UP -> {
                val x = event.x
                val y = event.y
                // need to center the touch point
                val correctedX = x - cacheGraphCenterX
                val correctedY = y - cacheGraphCenterY
                convertDecartToRadial(tempRectF, correctedX, correctedY)
                // need to understand if falls in correct distance from circle center
                val smallestRadius = cachedRadius * (1 - constTorWidthCoef)
                if (smallestRadius <= tempRectF.x() && tempRectF.x() <= cachedRadius) {
                    // need to understand the clicked sector now
                    val listToCheck =
                        if (groupByCategories) cacheCategoriesAmounts else cacheDataAmounts
                    val scalingFactor =
                        (360 - getElementsAmount() * getAngleGap()) / getTotalDataAmount()
                    val offsetCorrectedAngle = (tempRectF.y() - offsetAngle).let {
                        if (it < 0f) {
                            360f + it
                        } else {
                            it
                        }
                    }
                    val index = listToCheck.findSectorThatValueFallsIn(
                        ((offsetCorrectedAngle + getAngleGap()) / scalingFactor).toInt(),
                        gapAngle = getAngleGap(),
                        scalingFactor = scalingFactor,
                        startIndex = 1,
                        endIndex = listToCheck.size) - 1
                    if (index >= 0) {
                        val colorToChangeIndex = colorStack.getColorIndexGivenForIndex(index)
                        val colorToChange = colors[colorToChangeIndex]
                        Color.colorToHSV(colorToChange, colorValuesArr)
                        // increasing V value of hsv color
                        colorValuesArr[2] =
                            (colorValuesArr[2] + colorValuesArr[2] * 0.4f).coerceAtMost(1f)
                        val color = Color.HSVToColor(colorValuesArr)
                        colorStack.setAlternativeColorForIndex(index, color)
                        colorStack.reset()
                        val toDisplay = if (groupByCategories) {
                            cacheCategories[index]
                        } else {
                            data[index].name
                        }
                        currentlyDisplayedText = toDisplay
                        sectorClickListener?.onSectorClick(toDisplay)
                        invalidate()
                    }
                } else {
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // sorted ascending
    private fun List<Int>.findSectorThatValueFallsIn(
        valueToCheck: Int,
        gapAngle: Float,
        scalingFactor: Float,
        startIndex: Int,
        endIndex: Int,
    ): Int {
        val amountCoef = gapAngle / scalingFactor

        var i = startIndex
        while (i < endIndex) {
            val currentValue = get(i)
            if (currentValue + (amountCoef * i) > valueToCheck) {
                return i
            }
            i++
        }
        return -1
    }

    // ANIMATION
    private var currentAnimator: ValueAnimator? = null
    var interpolator: Interpolator = LinearInterpolator()
    private val floatEvaluator = FloatEvaluator()
    private var nextValue: Float? = null
    private var duration = 100L
    fun setOffset(offset: Float) {
        nextValue = offset
        restartAnimationWithRecentValue()

    }

    private fun restartAnimationWithRecentValue() {
        val nextValue = nextValue
        if (nextValue != null && currentAnimator == null) {
            currentAnimator = ValueAnimator.ofFloat(this.offsetAngle, nextValue)
                .apply {
                    interpolator = this@PieChartView.interpolator
                    setEvaluator(floatEvaluator)
                    this.duration = this@PieChartView.duration
                    addUpdateListener {
                        this@PieChartView.offsetAngle = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                    doOnEnd {
                        currentAnimator = null
                        restartAnimationWithRecentValue()
                    }
                }

        }
    }

    private class ColorStack(val shuffledColors: List<Pair<Int, Int>>) {
        private var currentIndex = 0
        private var alternativeColorIndex: Int? = null
        private var alternativeColor: Int? = null

        fun setAlternativeColorForIndex(index: Int, color: Int) {
            alternativeColor = color
            alternativeColorIndex = index
        }

        fun nextColor(): Int {
            if (currentIndex >= shuffledColors.size) {
                innerReset()
            }
            return if (currentIndex == alternativeColorIndex && alternativeColor != null) {
                currentIndex++
                checkNotNull(alternativeColor)
            } else {
                shuffledColors[currentIndex++].second
            }
        }

        fun reset() {
            innerReset()
        }

        fun getColorIndexGivenForIndex(index: Int): Int {
            return shuffledColors[index % shuffledColors.size].first
        }

        private fun innerReset() {
            currentIndex = 0
        }
    }

    interface SectorClickListener {
        fun onSectorClick(sectorName: String)
    }

    class SavedState : View.BaseSavedState {
        var offsetAngle: Float = 0f
        var groupByCategories: Boolean = true
        var seed: Long = 100L

        private constructor(source: Parcel) : super(source) {
            offsetAngle = source.readFloat()
            groupByCategories = source.readInt() != 0
            seed = source.readLong()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(offsetAngle)
            out.writeInt(if (groupByCategories) 1 else 0)
            out.writeLong(seed)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private fun String.toColor(): Int = Color.parseColor(this)

        // ui related constans
        protected val constTorWidthCoef = 0.33f
        protected val colors = listOf(
            /* teal 800 */
            "#ff00695c",
            /* purple 400 */
            "#ffab47bc",
            /* blue 500 */
            "#ff2196f3",
            /* cyan 600 */
            "#ff00acc1",
            /* red 400 */
            "#ffef5350",
            /* indigo 500 */
            "#ff3f51b5",
            /* green 400 */
            "#ff66bb6a",
            /* yello 500 */
            "#ffffeb3b",
            /* orange 400 */
            "#ffffa726",
            /* blue grey 400 */
            "#ff455a64",
        ).map { it.toColor() }
    }
}
