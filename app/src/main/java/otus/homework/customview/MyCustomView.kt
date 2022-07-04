package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.atan2
import kotlin.math.min
import kotlin.random.Random


class MyCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("myDebug", "onMeasure")

        val measureSpecWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureSpecHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec)

        Log.i("myDebug", "onMeasure: measureSpecWidthMode -> $measureSpecWidthMode")
        Log.i("myDebug", "onMeasure: measureSpecWidth -> $measureSpecWidth")
        Log.i("myDebug", "onMeasure: measureSpecHeightMode -> $measureSpecHeightMode")
        Log.i("myDebug", "onMeasure: measureSpecHeight -> $measureSpecHeight")

        val currentWidth: Int = when (measureSpecWidthMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecWidth
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
                measureSpecHeight
            }
            else -> throw IllegalStateException("Incorrect Height")
        }

        val minSideValue = min(currentWidth, currentHeight)

        setMeasuredDimension(minSideValue, minSideValue)
    }

    private val maxSegmentSize = 80f.toPx

    private val smallRadius = 80f.toPx

    private val x0
        get() = min(measuredHeight, measuredWidth).toFloat() / 2

    private val y0
        get() = min(measuredHeight, measuredWidth).toFloat() / 2

    private val arrayOfSegments = mutableListOf<Segment>()

    private val linePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRGB(255, 0, 0)

        arrayOfSegments.forEach {
            it.onDraw(
                x0 = x0,
                y0 = y0,
                smallRadius = smallRadius,
                paint = linePaint,
                canvas = canvas
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("myDebug", "onTouchEvent: x -> ${event.x}")
                Log.i("myDebug", "onTouchEvent: y -> ${event.y}")

                arrayOfSegments.forEach { segment ->
                    val isTouchInSegment = segment.isTouchInSegment(
                        xPoint = event.x,
                        yPoint = event.y,
                        bigRadius = smallRadius + segment.segmentWidth,
                        smallRadius = smallRadius,
                        x0 = x0,
                        y0 = y0
                    )

                    if (isTouchInSegment) {
                        val touchPointAngel = segment.isTouchInSegment1(event.x, event.y, x0, y0)
                        if (touchPointAngel) {
                            segment.color = generateRandomColor()
                            invalidate()
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

    fun setData(dataEntity: SegmentsDataEntity) {
        Log.i("myDebug", "onCreate: dataEntity -> $dataEntity")

        val sumOfAllCategories = dataEntity.data.sumOf { it.amount }
        val groupOfCategories = dataEntity.data.groupBy { it.category }
        val countOfDelimiters = groupOfCategories.size
        val sizeOfDelimitersInPercents = 1f
        val allSegmentSize = 100
        Log.i("myDebug", "setData: sumOfAllCategories -> $sumOfAllCategories")
        Log.i("myDebug", "setData: groupOfCategories -> $groupOfCategories")
        Log.i("myDebug", "setData: countOfGroups -> $countOfDelimiters")
        Log.i("myDebug", "setData: sizeOfDelimitersInPercents -> $sizeOfDelimitersInPercents")
        Log.i("myDebug", "setData: allSegmentSize -> $allSegmentSize")
        val sumOfEachCategories = groupOfCategories.mapValues {
            it.value.sumOf { items -> items.amount }
        }
        val eachCategoriesInPercents =
            sumOfEachCategories.mapValues { it.value * allSegmentSize / 100f * 1 / sumOfAllCategories }
        val sumOfCategories2 = eachCategoriesInPercents.map { it.value }.sum()

        Log.i("myDebug", "setData: sumOfEachCategories -> $sumOfEachCategories")
        Log.i("myDebug", "setData: eachCategoriesInPercents -> $eachCategoriesInPercents")
        Log.i("myDebug", "setData: sumOfCategories2 -> $sumOfCategories2")


        val eachCategoriesInDegrees = eachCategoriesInPercents.mapValues { it.value * 360 }
        val eachCategoriesInDegreesSum = eachCategoriesInPercents.map { it.value }.sum()
        Log.i("myDebug", "setData: eachCategoriesInDegrees -> $eachCategoriesInDegrees")
        Log.i("myDebug", "setData: eachCategoriesInDegreesSum -> $eachCategoriesInDegreesSum")

        eachCategoriesInDegrees.map { it.value }.fold(0f) { acc, fl ->
            Log.i("myDebug", "setData: acc -> $acc, fl -> $fl")
            val result = acc + fl

            val segmentUIEntity = Segment(
                startAngel = acc,
                endAngel = result - 3.6f * sizeOfDelimitersInPercents,
                segmentWidth = 0f.toPx
            )

            Log.i("myDebug", "setData: segmentUIEntity -> $segmentUIEntity")
            arrayOfSegments.add(segmentUIEntity)

            result
        }

        eachCategoriesInPercents.map { it.value }.forEachIndexed { index, fl ->
            arrayOfSegments[index].segmentWidth = maxSegmentSize * (fl + 0.4f)
        }

    }
}

data class Segment(
    private val startAngel: Float,
    private val endAngel: Float,
    var segmentWidth: Float,
    @ColorInt var color: Int = generateRandomColor()
) {
    private val path = Path()
    private val blurFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.SOLID)

    fun onDraw(
        x0: Float,
        y0: Float,
        smallRadius: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        path.reset()
        path.addArc(
            x0 - smallRadius - segmentWidth / 2,
            y0 - smallRadius - segmentWidth / 2,
            x0 + smallRadius + segmentWidth / 2,
            y0 + smallRadius + segmentWidth / 2,
            startAngel, endAngel - startAngel
        )

        paint.color = color
        paint.strokeWidth = segmentWidth
        paint.maskFilter = blurFilter

        canvas.drawPath(path, paint)
    }

    fun isTouchInSegment1(
        xPoint: Float,
        yPoint: Float,
        x0Point: Float,
        y0Point: Float
    ): Boolean {
        val angel = getAngleFromTouch(
            xPoint = xPoint,
            yPoint = yPoint,
            x0Point = x0Point,
            y0Point = y0Point
        )

        return angel in startAngel..endAngel
    }

    fun isTouchInSegment(
        xPoint: Float,
        yPoint: Float,
        bigRadius: Float,
        smallRadius: Float,
        x0: Float,
        y0: Float
    ): Boolean {
        val dX = xPoint - x0
        val dY = yPoint - y0

        return (dX * dX + dY * dY) < bigRadius * bigRadius
                && (dX * dX + dY * dY) > smallRadius * smallRadius
    }

    private fun getAngleFromTouch(
        xPoint: Float,
        yPoint: Float,
        x0Point: Float,
        y0Point: Float
    ): Float {
        val dX = (x0Point - xPoint).toDouble()
        val dY = (y0Point - yPoint).toDouble()

        val angelInRadians = atan2(dY, dX)
        val angelInDegrees = angelInRadians * 180 / Math.PI

        return angelInDegrees.toFloat() + 180
    }
}

fun generateRandomColor(): Int =
    Color.argb(
        255,
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density

val Float.toDp get() = this / Resources.getSystem().displayMetrics.density


val Int.toPx get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.toDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()