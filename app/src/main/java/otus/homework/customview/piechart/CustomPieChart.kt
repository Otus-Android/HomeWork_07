package otus.homework.customview.piechart

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlinx.parcelize.Parcelize
import otus.homework.customview.dp
import otus.homework.customview.generateRandomColor
import kotlin.math.min

class CustomPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentState: CustomPieChartState = CustomPieChartState.NOT_INIZIALIZED

    private var onSegmentClick: ((Segment) -> Unit)? = null

    private val maxSegmentSize
        get() = min(measuredWidth, measuredHeight) / 6f

    private val smallRadius
        get() = min(measuredWidth, measuredHeight) / 5f

    private val x0
        get() = measuredWidth / 2f

    private val y0
        get() = measuredHeight / 2f

    private val segmentsPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val circleColor = Color.CYAN
    private val circlePath = Path()
    private val circlePaint = Paint().apply {
        color = circleColor
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val circleOffset = 5f.dp

    private val amountTextPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 24f.dp
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val monthTextPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 14f.dp
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val measureSpecWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureSpecHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec)

        val currentWidth: Int = when (measureSpecWidthMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                min(measureSpecWidth, measureSpecHeight)
            }
            else -> throw IllegalStateException("Incorrect Width")
        }

        val currentHeight: Int = when (measureSpecHeightMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecHeight
            }
            MeasureSpec.AT_MOST -> {
                measureSpecHeight
            }
            MeasureSpec.UNSPECIFIED -> {
                min(measureSpecWidth, measureSpecHeight)
            }
            else -> throw IllegalStateException("Incorrect Height")
        }

        setMeasuredDimension(currentWidth, currentHeight)
    }

    override fun onDraw(canvas: Canvas) {
        currentState.segments.forEach {
            it.onDraw(
                x0 = x0,
                y0 = y0,
                maxSegmentWidth = maxSegmentSize,
                smallRadius = smallRadius,
                paint = segmentsPaint,
                canvas = canvas
            )
        }

        drawCentralCircle(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                currentState.segments.forEach { segment ->
                    val isTouchInSegmentRange = segment.isTouchInSegmentRange(
                        xPoint = event.x,
                        yPoint = event.y,
                        smallRadius = smallRadius,
                        x0 = x0,
                        y0 = y0
                    )

                    if (isTouchInSegmentRange) {
                        val isTouchInCurrentSegment =
                            segment.isTouchInCurrentSegment(
                                xPoint = event.x,
                                yPoint = event.y,
                                x0Point = x0,
                                y0Point = y0
                            )
                        if (isTouchInCurrentSegment) {
                            onSegmentClick(segment = segment)
                        }
                    }
                }
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return BaseSavedState(currentState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        currentState = (state as BaseSavedState).superState as CustomPieChartState
        super.onRestoreInstanceState(state)
    }

    fun setData(dataEntity: SegmentsDataEntity) {
        val sizeOfDelimitersInPercents = 1f
        val allSegmentSize = 100
        val degreesInCircle = 360f

        val segments = mutableListOf<Segment>()
        val currentMonthText = "In ${dataEntity.month}"
        val amountOfAllCategories = dataEntity.data.sumOf { it.amount }
        val amountOfAllCategoriesText = "$ $amountOfAllCategories"

        val groupOfCategories = dataEntity.data.groupBy { it.category }

        val amountOfEachCategories = groupOfCategories.mapValues {
            it.value.sumOf { items -> items.amount }
        }

        val eachCategoriesInPercents = amountOfEachCategories
            .mapValues { it.value * allSegmentSize / 100f / amountOfAllCategories }

        val eachCategoriesInDegrees = eachCategoriesInPercents
            .mapValues { it.value * degreesInCircle }

        eachCategoriesInDegrees.map { it.value }.fold(0f) { acc, fl ->
            val result = acc + fl

            val segmentUIEntity = Segment(
                startAngel = acc,
                endAngel = result - (degreesInCircle * sizeOfDelimitersInPercents / 100)
            )

            segments.add(segmentUIEntity)

            result
        }

        groupOfCategories.keys.forEachIndexed { index, value ->
            segments[index].category = value
        }

        eachCategoriesInPercents.map { it.value }.forEachIndexed { index, fl ->
            segments[index].percents = fl * 100f
        }

        currentState = CustomPieChartState.INIZIALIZED(
            segments = segments,
            amountOfAllCategoriesText = amountOfAllCategoriesText,
            currentMonthText = currentMonthText
        )
    }

    fun setOnSegmentClickListener(callBack: (Segment) -> Unit) {
        onSegmentClick = callBack
    }

    private fun onSegmentClick(segment: Segment) {
        ValueAnimator
            .ofArgb(segment.color, generateRandomColor())
            .apply {
                addUpdateListener {
                    val color = it.animatedValue as Int
                    segment.color = color
                    invalidate()
                }
                duration = 200
            }
            .start()

        ValueAnimator
            .ofFloat(
                segment.offsetOfPercentText,
                segment.offsetOfPercentText + 20f.dp
            )
            .apply {
                interpolator = LinearInterpolator()
                duration = 200
                repeatCount = 1
                repeatMode = ValueAnimator.REVERSE
                addUpdateListener {
                    val value = it.animatedValue as Float
                    segment.offsetOfPercentText = value
                    invalidate()
                }
            }
            .start()

        onSegmentClick?.invoke(segment)
    }

    private fun drawCentralCircle(canvas: Canvas) {
        circlePath.addCircle(x0, y0, smallRadius - circleOffset, Path.Direction.CW)
        canvas.drawPath(circlePath, circlePaint)
        canvas.drawText(currentState.amountOfAllCategoriesText, x0, y0, amountTextPaint)
        canvas.drawText(currentState.currentMonthText, x0, y0 + 18f.dp, monthTextPaint)
    }

    private sealed class CustomPieChartState(
        open val segments: List<Segment>,
        open val amountOfAllCategoriesText: String,
        open val currentMonthText: String,
    ) : Parcelable {

        @Parcelize
        object NOT_INIZIALIZED :
            CustomPieChartState(mutableListOf(), "", "")

        @Parcelize
        class INIZIALIZED(
            override val segments: List<Segment>,
            override val amountOfAllCategoriesText: String,
            override val currentMonthText: String,
        ) : CustomPieChartState(segments, amountOfAllCategoriesText, currentMonthText)
    }
}