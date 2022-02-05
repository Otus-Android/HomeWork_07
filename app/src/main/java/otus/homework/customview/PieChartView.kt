package otus.homework.customview

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
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
)

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    val path = Path()

    private var tempF = RectF()
    val tempRect = Rect()
    private var torWidthCoef = 0.33f
    private var gaps = (8 * resources.displayMetrics.density)
    private var data: List<ChartData> = listOf(
        ChartData(300, "1", 1, ""),
        ChartData(500, "2", 2, ""),
        ChartData(200, "3", 3, ""),

        )
    private var displayHeight = context.resources.displayMetrics.run { heightPixels }
    private var displayWidth = context.resources.displayMetrics.run { widthPixels }
    private var offsetAngle = 0f

    private val cacheDataAmounts: MutableList<Int> = mutableListOf()
    private var cacheDataAmountSum: Int = 1
    private var cacheGraphWidth = 0
    private var cacheGraphHeight = 0
    private val cacheGraphDrawArea = RectF()
    private var cachedRadius: Float = 1f
    private var cachedGapAngleBetweenArcs: Float = 1f

    private fun String.toColor(): Int = Color.parseColor(this)
    private val colors = listOf(
        /* teal 800 */
        "#ff00695c".toColor(),
        /* purple 400 */
        "#ffab47bc".toColor(),
        /* blue 500 */
        "#ff2196f3".toColor(),
        /* cyan 600 */
        "#ff00acc1".toColor(),
        /* red 400 */
        "#ffef5350".toColor(),
        /* indigo 500 */
        "#ff3f51b5".toColor(),
        /* green 400 */
        "#ff66bb6a".toColor(),
        /* yello 500 */
        "#ffffeb3b".toColor(),
        /* orange 400 */
        "#ffffa726".toColor(),
        /* blue grey 400 */
        "#ff455a64".toColor(),
    )
    private var seed = System.currentTimeMillis()
    private var colorStack = ColorStack(colors.shuffled(Random(seed)))

    private class ColorStack(val shuffledColors: List<Int>) {
        private var currentIndex = 0
        fun takeColor(): Int {
            if (currentIndex >= shuffledColors.size) {
                reset()
            }
            return shuffledColors[currentIndex++]
        }

        fun reset() {
            currentIndex = 0
        }
    }

    init {
        setData(data)
        Log.d("FOCK", "gaps = $gaps")
    }

    fun setData(data: List<ChartData>) {
        this.data = data
        cacheDataAmountSum = data.sumBy {
            it.amount
        }
        cacheDataAmounts.clear()
        cacheDataAmounts.addAll(
            data.runningFold(0) { left, right ->
                left + right.amount
            }
        )
        Log.d("FOCK", "data is set: $cacheDataAmounts")
    }

    private fun convertDecartToRadial(out: RectF, x: Float, y: Float) {
        val angle = kotlin.math.atan(x / y) / 3.14f * 360f
        val distance = sqrt(x.pow(2) + y.pow(2))
        out.setXY(distance, angle)
    }

    private fun convertRadialToDecart(out: RectF, ro: Float, phi: Float) {
        val x = ro * cos(phi)
        val y = ro * sin(phi)
        out.setXY(x, y)
    }

    private fun RectF.setXY(x: Float, y: Float) {
        set(x, y, 0f, 0f)
    }

    private fun RectF.x() = left
    private fun RectF.y() = top

    private val paint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.FILL
            textSize = 17 * resources.displayMetrics.scaledDensity
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec);
        val heightMode = MeasureSpec.getMode(heightMeasureSpec);
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minDisplaySize = displayWidth.coerceAtMost(displayHeight)
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = this.width
        val height = this.height
        cacheGraphWidth = this.width - (paddingStart) - paddingEnd
        cacheGraphHeight = height - paddingTop - paddingBottom
        val min = cacheGraphWidth.coerceAtMost(cacheGraphHeight)
        val radius = min / 2f
        val left = (this.width / 2f) - radius
        val right = left + 2 * radius
        val top = (height / 2f) - radius
        val bottom = top + 2 * radius
        cacheGraphDrawArea.set(left, top, right, bottom)
        cachedRadius = radius
        cachedGapAngleBetweenArcs = gaps / radius * 180f / 3.14f
        Log.d("FOCK", "cachedRadius: ${cachedRadius}, cachedGapAngle : $cachedGapAngleBetweenArcs")
    }

    private fun getTotalDataAmount(): Int {
        return cacheDataAmountSum
    }

    private fun Canvas.drawArc(
        dataIndex: Int,
        scalingFactor: Float,
        circleRect: RectF,
        gapSize: Float,
        paint: Paint,
    ) {
        if (cacheDataAmounts.isEmpty()) return
        if (cacheDataAmounts.size <= dataIndex || dataIndex < 0) return

        val startingAngle =
            cacheDataAmounts[dataIndex] * scalingFactor + dataIndex * gapSize + offsetAngle
        val sweepAngle =
            (cacheDataAmounts[dataIndex + 1] - cacheDataAmounts[dataIndex].toFloat()) * scalingFactor

        drawArc(circleRect, startingAngle, sweepAngle, true, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()
        colorStack.reset()
        val radius = cachedRadius
        val totalAmountToDisplay = getTotalDataAmount()
//        val gapAngle = atan((gaps / 2f) / (sqrt(radius.pow(2) - (gaps / 2f).pow(2))) ) / 3.14f * 360
        val gapAngle = cachedGapAngleBetweenArcs
//        Log.d("FOCK",
//            "gapAngle: $gapAngle, gap distanc: ${gapAngle * 3.14 / 180 * radius} radius: $radius, 16dp ${16 * context.resources.displayMetrics.density}")
        val scalingFactor = (360 - data.size * gapAngle) / totalAmountToDisplay
        val innerRadius = (1 - torWidthCoef) * radius

        canvas.save()
        if (Build.VERSION.SDK_INT >= P) {
            canvas.clipOutPath(path.apply {
                addCircle(this@PieChartView.width / 2f,
                    height / 2f,
                    innerRadius,
                    Path.Direction.CCW)
            })
        } else {
            canvas.clipPath(path.apply {
                addCircle(this@PieChartView.width / 2f,
                    height / 2f,
                    innerRadius,
                    Path.Direction.CCW)
            }, Region.Op.DIFFERENCE)
        }
        var i = 0
        while (i < data.size) {
            canvas.drawArc(i,
                scalingFactor,
                cacheGraphDrawArea,
                cachedGapAngleBetweenArcs,
                paint.apply {
                    color = colorStack.takeColor()
                })
            i++
        }
        canvas.restore()
        val text = "CLIPPING"
        paint.getTextBounds(text, 0, text.length, tempRect)

        val centerX = this.width / 2f
        val centerY = this.height / 2f

        canvas.drawRect(centerX - tempRect.width() / 2f, centerY - tempRect.height() / 2f,
            centerX + tempRect.width() / 2f, centerY + tempRect.height() / 2f, paint.apply {
                color = Color.WHITE
            })

        canvas.drawText("CLIPPING",
            this.width / 2f - (tempRect.width() / 2f),
            this.height / 2f + (tempRect.height() / 2f), paint.apply {
                color = Color.BLUE
            })
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    // ANIMATION
    private var currentAnimator: ValueAnimator? = null
    var interpolator: Interpolator = LinearInterpolator()
    private val floatEvaluator = FloatEvaluator()
    private var nextValue: Float? = null

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
                    this.duration = 100
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
}
