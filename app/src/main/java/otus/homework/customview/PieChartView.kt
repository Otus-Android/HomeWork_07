package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.*;
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

    private var startingAngle: Float = 0f
    private var tempF = RectF()
    private var torWidthCoef = 0.33f
    private var gaps = (8 * resources.displayMetrics.scaledDensity)
    init {
        Log.d("FOCK", "gaps = $gaps")
    }

    private var data: List<ChartData> = listOf(
        ChartData(300, "1", 1, ""),
        ChartData(500, "2", 2, ""),
        ChartData(200, "3", 3, ""),

        )
    private var displayHeight = context.resources.displayMetrics.run { heightPixels }
    private var displayWidth = context.resources.displayMetrics.run { widthPixels }

    private var cacheDataAmountSum: Int = 1

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
    }

    fun setData(data: List<ChartData>) {
        this.data = data
        cacheDataAmountSum = data.sumBy { it.amount }
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

    private fun getTotalDataAmount(): Int {
        return cacheDataAmountSum
    }

    private val clearXFer = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val defalutXFer = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        colorStack.reset()

        val graphWidth = this.width - (paddingStart) - paddingEnd
        val graphHeight = height - paddingTop - paddingBottom
        val min = graphWidth.coerceAtMost(graphHeight)
        val radius = min / 2f
        var i = 0
        val left = (this.width / 2f) - radius
        val right = left + 2 * radius
        val top = (height / 2f) - radius
        val bottom = top + 2 * radius

        val totalAmountToDisplay = getTotalDataAmount()
        val gapAngle = 2 * atan(gaps / 2f / (sqrt(radius.pow(2) - (gaps / 2f).pow(2))) ) / 3.14f * 360
//        val gapAngle = 0f
        Log.d("FOCK", "gapAngle: $gapAngle, radius: $radius")
        val scalingFactor = (360 - data.size * gapAngle) / totalAmountToDisplay

        var currentAngle = startingAngle
        paint.apply {
            xfermode = defalutXFer
        }
        while (i < data.size) {
            val deltaAngle = data[i].amount * scalingFactor
            canvas.drawArc(left, top, right, bottom, currentAngle, deltaAngle, true, paint.apply {
                color = colorStack.takeColor()
            })
            currentAngle += deltaAngle + gapAngle
            i++
        }
        val innerRadius = (1 - torWidthCoef) * radius
        canvas.drawCircle(this.width / 2f, height / 2f, innerRadius , paint.apply {
            xfermode = clearXFer
        })
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }
}
