package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val TAG = "PieChart"

//private const val BASE_CONTENT_SIZE_DP = 200F
private const val CHART_THICKNESS_DP = 60f
private const val CHART_DIAMETER = 200F

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private var baseContentSizePx: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        CHART_DIAMETER + CHART_THICKNESS_DP / 2,
        resources.displayMetrics
    ).roundToInt()

    private var thicknessPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        CHART_THICKNESS_DP,
        resources.displayMetrics
    )

    private var halfThicknessPx = thicknessPx / 2

    private var diameterPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        CHART_DIAMETER,
        resources.displayMetrics
    )

    private val chartPaint = Paint().apply {
        this.color = Color.GREEN
        this.strokeWidth = thicknessPx
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        this.isAntiAlias = true
        this.style = Paint.Style.FILL
        this.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
    }
    private val percentPaint = Paint(textPaint)

    private val colors = listOf<Int>(
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.MAGENTA,
        Color.CYAN,
        Color.YELLOW,
        0xFFFF8000.toInt(),
        0xFF00FF80.toInt(),
        0xFF8000FF.toInt(),
        Color.GRAY
    )

    private data class Sector(
        var angle: Float,
        var category: String,
        var percentText: String,
        var percentX: Float,
        var percentY: Float,
        var sum: Int,
    )

    private var sectors = listOf<Sector>()

    private var selectListener: ((category: String) -> Unit)? = null

    private val chartRect = RectF(halfThicknessPx, halfThicknessPx, diameterPx + halfThicknessPx, diameterPx + halfThicknessPx)

    private var legendLineHeight: Float
    private var percentHalfWidth: Float
    private var percentHalfHeight: Float

    init {
        // Load attributes
        context.obtainStyledAttributes(
            attrs, R.styleable.PieChart, defStyleAttr, 0
        ).apply {
            this.recycle()
        }

        baseContentSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            CHART_DIAMETER + CHART_THICKNESS_DP,
            resources.displayMetrics
        ).roundToInt()

        val fontMetrics = textPaint.fontMetrics
        legendLineHeight = fontMetrics.descent - fontMetrics.ascent
        percentHalfWidth = textPaint.measureText("100%") / 2
        percentHalfHeight = legendLineHeight / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawColor(Color.GREEN)

        var fullAngle = 0f
        for (i in 0..sectors.lastIndex) {
            val color = colors[i]
            val sector = sectors[i]
            val (angle, category) = sector
            chartPaint.color = color
            canvas.drawArc(chartRect, fullAngle, angle, false, chartPaint)
            fullAngle += angle

            canvas.drawText(sector.percentText, sector.percentX, sector.percentY, percentPaint)
            textPaint.color = color
            canvas.drawText(category, 0f, chartRect.bottom + halfThicknessPx + (i + 1) * legendLineHeight, textPaint)
        }

        canvas.drawLine(chartRect.left, chartRect.centerY(), chartRect.right, chartRect.centerY(), Paint().apply { color = Color.BLACK })

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "called onMeasure()")

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)


        val width = calculateDimension(wMode, wSize)
        val height = calculateDimension(hMode, hSize)

//        val width = View.resolveSize(baseContentSizePx, widthMeasureSpec)
//        val height = View.resolveSize(baseContentSizePx, heightMeasureSpec)

        setMeasuredDimension(width, height + (legendLineHeight * sectors.size).roundToInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        Log.d(TAG, "called onSizeChanged()")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d(TAG, "called onLayout()")
    }

    private fun calculateDimension(mode: Int, size: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(baseContentSizePx, size)
            MeasureSpec.UNSPECIFIED -> baseContentSizePx
            else -> size
        }
    }

    fun setOnSelectListener(selectListener: ((category: String) -> Unit)?) {
        this.selectListener = selectListener
    }

    fun hasOnSelectListener() = selectListener != null

    fun setCharges(charges: List<Charge>) {
        val categoryies = charges.groupBy { it.category }
        val sumAmount = charges.sumOf { it.amount }
        val result: List<Sector> = charges
            .groupBy { it.category }
            .mapValues { it.value.sumOf { charge -> charge.amount } }
            .map {
                val radian = it.value.toDouble() * PI * 2 / sumAmount
                Sector(
                    it.value.toFloat() * 360 / sumAmount,
                    it.key,
                    "${it.value * 100 / sumAmount}%",
                    0f,
                    0f,
                    it.value,
                )
            }
            .sortedByDescending { it.sum }
            .take(10)

        var fullAngle = 0.0
        for (sector in result) {
            val textHalfWidth = textPaint.measureText(sector.percentText) / 2
            val radian = (fullAngle + sector.angle / 2) / 180.0 * PI
            sector.percentX = (chartRect.centerX() + cos(radian) * (chartRect.width() / 2) - textHalfWidth).toFloat()
            sector.percentY = (chartRect.centerY() + sin(radian) * (chartRect.height() / 2) + percentHalfHeight).toFloat()
            fullAngle += sector.angle
        }

        sectors = result
        invalidate()

//        val result = mutableListOf<Pair<Float, String>>()
//        for ((category, charge) in categoryies.entries) {
//            result
//        }
    }
}