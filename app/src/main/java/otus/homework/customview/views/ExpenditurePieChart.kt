package otus.homework.customview.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.ColorUtils
import otus.homework.customview.models.PieChartSegment
import otus.homework.customview.R
import kotlin.math.*

class ExpenditurePieChart : View {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val minPieChartSize = resources.getDimension(R.dimen.pie_chart_min_size).toInt()

    private var rotationAngle = 0f
    private var increasingDelta = 0f
    private var decreasingAlpha = 0
    private var decreasingDelta = 0f
    private var increasingAlpha = 0

    private val globalRect = Rect()
    private val outerRectF = RectF()
    private val innerRectF = RectF()
    private val animationDelta: Float by lazy { (outerRectF.width() - innerRectF.width()) / 2f / DEFAULT_COEFFICIENT }
    private val percentageRectF = RectF()

    private val pieChartPath = Path()
    private val nextStartPointPath = Path()
    private val pathMeasure = PathMeasure()
    private val startPoint = PointF()
    private val nextStartPoint = PointF()

    private val mainPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 2f
    }
    private val whitePaint = Paint().apply {
        color = Color.rgb(255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val moneyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 72f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val signaturePaint = Paint().apply {
        color = Color.GRAY
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val cornerPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GRAY
        strokeWidth = 2f
        alpha = 50
    }

    private lateinit var segments: List<PieChartSegment>
    private var increasingSegment: PieChartSegment? = null
    private var decreasingSegment: PieChartSegment? = null
    private var callback: ((String) -> Unit)? = null

    private val generalGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                return handleOnSingleTapUp(event)
            }
        })


    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(segments, superState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            segments = state.segments
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return generalGestureDetector.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewWidth = measureViewSize(widthMeasureSpec)
        val viewHeight = measureViewSize(heightMeasureSpec)
        min(viewWidth, viewHeight).also { viewSize ->
            setMeasuredDimension(viewSize, viewSize)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calculateRect(canvas)


        startPoint.set(innerRectF.right, innerRectF.centerY())

        segments.forEach { segment ->
            /*
            * Adjustment of the segment width according to the ratio with the highest value of the diagram
            * */
            val ratio = (1 - segment.percentageOfMaximum / 100f)
            val realCoefficient = ratio / DEFAULT_COEFFICIENT
            var dx = (outerRectF.width() - innerRectF.width()).div(2).times(realCoefficient)
            var dy = (outerRectF.height() - innerRectF.height()).div(2).times(realCoefficient)

            when (segment.id) {
                increasingSegment?.id -> {
                    dx += increasingDelta
                    dy += increasingDelta
                }
                decreasingSegment?.id -> {
                    dx += decreasingDelta
                    dy += decreasingDelta
                }
            }
            outerRectF.inset(dx, dy)

            calculatePieChartPath(segment)
            calculateNextStartPoint(segment)

            mainPaint.apply {
                color = segment.color
                /*
                * TODO(Don't know how to change the coordinates of a LinearGradient without recreating its entity in onDraw)
                * */
                shader = LinearGradient(
                    startPoint.x,
                    startPoint.y,
                    nextStartPoint.x,
                    nextStartPoint.y,
                    segment.color,
                    ColorUtils.setAlphaComponent(segment.color, ALPHA_CHANNEL_FOR_THE_GRADIENT),
                    Shader.TileMode.MIRROR
                )
            }

            drawPercentageItem(canvas, segment)
            drawSegmentItem(canvas)

            outerRectF.inset(dx.unaryMinus(), dy.unaryMinus())

            pieChartPath.reset()
            startPoint.set(nextStartPoint)
        }

        drawCentralSignature(canvas)
    }

    fun setupData(segments: List<PieChartSegment>, callback: (String) -> Unit) {
        this.segments = segments
        this.callback = callback
    }

    fun startAnimation() {
        ValueAnimator.ofFloat(0f, 145f).apply {
            setDuration(1500)
            setInterpolator(OvershootInterpolator())
            addUpdateListener {
                rotationAngle = animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startSegmentIncreaseAnim(segment: PieChartSegment) {
        increasingSegment = segment
        val increaseAnimator = ValueAnimator.ofFloat(0f, animationDelta.unaryMinus()).apply {
            setDuration(1000)
            setInterpolator(OvershootInterpolator())
            addUpdateListener {
                increasingDelta = animatedValue as Float
                invalidate()
            }
        }
        val alphaAnimator = ValueAnimator.ofInt(255, 0).apply {
            setDuration(1000)
            addUpdateListener {
                decreasingAlpha = it.animatedValue as Int
            }
        }
        AnimatorSet().apply {
            playTogether(increaseAnimator, alphaAnimator)
            start()
        }
    }

    private fun startSegmentDecreaseAnim(segment: PieChartSegment) {
        decreasingSegment = segment
        val decreaseAnimator = ValueAnimator.ofFloat(animationDelta.unaryMinus(), 0f).apply {
            setDuration(1000)
            setInterpolator(OvershootInterpolator())
            addUpdateListener {
                decreasingDelta = animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    decreasingSegment = null
                }
            })
        }
        val alphaAnimator = ValueAnimator.ofInt(0, 255).apply {
            setDuration(1000)
            addUpdateListener {
                increasingAlpha = it.animatedValue as Int
            }
        }
        AnimatorSet().apply {
            playTogether(decreaseAnimator, alphaAnimator)
            start()
        }
    }

    private fun handleOnSingleTapUp(event: MotionEvent): Boolean {
        val clickInsideOuterRect = outerRectF.contains(event.x, event.y)
        val clickInsideInnerRect = innerRectF.contains(event.x, event.y)

        return when {
            clickInsideOuterRect && !clickInsideInnerRect -> {
                /*
                * segment click handling
                * */

                val dx = event.x - innerRectF.centerX().toDouble()
                val dy = -(event.y - innerRectF.centerY()).toDouble()
                var inRad = atan2(dy, dx)

                // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
                inRad = if (inRad < 0) abs(inRad) else 2 * Math.PI - inRad

                val inDegrees = Math.toDegrees(inRad)
                val trueAngle =
                    if (inDegrees >= rotationAngle) inDegrees - rotationAngle else 360 + inDegrees - rotationAngle
                segments.find { segment -> (segment.startAngle..segment.endAngle).contains(trueAngle) }
                    ?.let {
                        callback?.invoke(it.category)
                        if (it.id == increasingSegment?.id) {
                            increasingSegment = null
                            startSegmentDecreaseAnim(it)
                        } else {
                            increasingSegment
                                ?.let { startSegmentDecreaseAnim(it) }
                                .also { increasingSegment = null }
                            startSegmentIncreaseAnim(it)
                        }
                        true
                    } ?: false
            }
            else -> false
        }
    }

    private fun measureViewSize(measureSpec: Int): Int {
        var result = minPieChartSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = minPieChartSize
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = max(minPieChartSize, specSize)
        }
        return result
    }

    private fun calculateRect(canvas: Canvas) {
        canvas.getClipBounds(globalRect)

        globalRect.also { rect ->
            outerRectF.set(rect)
            innerRectF.set(rect)

            val width = globalRect.width()
            val height = globalRect.height()

            outerRectF.inset(width * 0.15f, height * 0.15f)
            innerRectF.inset(width * 0.3f, height * 0.3f)
        }
    }

    private fun calculatePieChartPath(segments: PieChartSegment) {
        with(segments) {
            pieChartPath.apply {
                moveTo(startPoint.x, startPoint.y)
                arcTo(innerRectF, startAngle, segments.segmentAngle)
                arcTo(outerRectF, endAngle, 0f)
                arcTo(outerRectF, endAngle, segments.segmentAngle.unaryMinus())
                arcTo(innerRectF, startAngle, 0f)
            }
        }
    }

    private fun calculateNextStartPoint(segment: PieChartSegment) {
        nextStartPointPath.apply {
            moveTo(startPoint.x, startPoint.y)
            arcTo(innerRectF, segment.startAngle, segment.segmentAngle)

            pathMeasure.setPath(this, false)
            val xyCoordinate = floatArrayOf(startPoint.x, startPoint.y)
            pathMeasure.getPosTan(pathMeasure.length, xyCoordinate, null)

            nextStartPoint.set(xyCoordinate[0], xyCoordinate[1])
            reset()
        }
    }


    /*
    * Draw methods
    * */

    private fun drawSegmentItem(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotationAngle, innerRectF.centerX(), innerRectF.centerY())
        canvas.drawPath(pieChartPath, mainPaint)
        canvas.drawPath(pieChartPath, whitePaint)
        canvas.restore()
    }

    private fun drawPercentageItem(
        canvas: Canvas,
        segment: PieChartSegment
    ) {
        val value = segment.segmentAngle * 100 / 360
        if (value < 3) return
        val angleInRadian =
            ((segment.endAngle + segment.startAngle) / 2 + rotationAngle) * PI / 180
        val distance = outerRectF.width() / 2 + 100
        val x = distance * cos(angleInRadian).toFloat() + outerRectF.centerX()
        val y = distance * sin(angleInRadian).toFloat() + outerRectF.centerY()
        percentageRectF.set(
            x - PERCENTAGE_HALF_WIDTH,
            y - PERCENTAGE_HALF_HEIGHT,
            x + PERCENTAGE_HALF_WIDTH,
            y + PERCENTAGE_HALF_HEIGHT
        )
        val defaultCornerAlpha = cornerPaint.alpha
        val defaultSignatureAlpha = signaturePaint.alpha
        cornerPaint.alpha = when (segment.id) {
            increasingSegment?.id -> decreasingAlpha / 5
            decreasingSegment?.id -> increasingAlpha / 5
            else -> defaultCornerAlpha
        }
        signaturePaint.alpha = when (segment.id) {
            increasingSegment?.id -> decreasingAlpha
            decreasingSegment?.id -> increasingAlpha
            else -> defaultSignatureAlpha
        }

        canvas.drawRect(percentageRectF, cornerPaint)
        val stringValue = value.toString().substringBefore(".")
        canvas.drawText("$stringValue%", x, y + 12, signaturePaint)

        cornerPaint.alpha = defaultCornerAlpha
        signaturePaint.alpha = defaultSignatureAlpha
    }

    private fun drawCentralSignature(canvas: Canvas) {
        val totalAmount = segments.fold(0f) { acc, segment ->
            acc.plus(segment.amount)
        }.let { total -> return@let "\$$total" }
        canvas.drawText(totalAmount, innerRectF.centerX(), innerRectF.centerY(), moneyPaint)

        val periodText = "in january"
        canvas.drawText(
            periodText,
            innerRectF.centerX(),
            innerRectF.centerY() + 56f,
            signaturePaint
        )
    }

    internal class SavedState : BaseSavedState {

        val segments: List<PieChartSegment>

        constructor(segments: List<PieChartSegment>, superState: Parcelable?) : super(superState) {
            this.segments = segments
        }

        private constructor(input: Parcel) : super(input) {
            segments = listOf()
            input.readList(segments, List::class.java.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(segments)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }


    companion object {
        private const val DEFAULT_COEFFICIENT = 1.66f
        private const val ALPHA_CHANNEL_FOR_THE_GRADIENT = 200
        private const val PERCENTAGE_HALF_WIDTH = 44
        private const val PERCENTAGE_HALF_HEIGHT = 32
    }
}