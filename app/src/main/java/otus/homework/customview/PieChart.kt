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
import kotlin.math.min
import kotlin.math.roundToInt

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

    private val paint = Paint().apply {
        this.color = Color.GREEN
        this.strokeWidth = thicknessPx
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE

    }

    private val textPaint = Paint().apply {
        this.isAntiAlias = true
        this.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
    }

    private var color1 = Color.RED
    private var color2 = Color.BLUE
    private var color3 = Color.GREEN
    private var color4 = Color.MAGENTA
    private var color5 = Color.CYAN
    private var color6 = Color.YELLOW

    private val colors = listOf<Int>(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW,
        0xFFFFFF00.toInt(),
        0xFF00FFFF.toInt(),
        0xFFFF00FF.toInt(), Color.GRAY)

    private var sectors = listOf<Triple<Float, String, String>>()

    private var selectListener: ((category: String) -> Unit)? = null

    private val chartRect = RectF(halfThicknessPx, halfThicknessPx, diameterPx + halfThicknessPx, diameterPx + halfThicknessPx)

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
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawColor(Color.GREEN)

        var fullAngle = 0f
        for (i in 0..sectors.lastIndex) {
            val color = colors[i]
            val (angle) = sectors[i]
            paint.color = color
            canvas.drawArc(chartRect, fullAngle, angle, false, paint)
            fullAngle += angle
        }

        canvas.drawLine(chartRect.left, chartRect.centerY(), chartRect.right, chartRect.centerY(), Paint().apply { color = Color.BLACK })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "called onMeasure()")

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)


//        val width = calculateDimension(wMode, wSize)
//        val height = calculateDimension(hMode, hSize)

        val width = View.resolveSize(baseContentSizePx, widthMeasureSpec)
        val height = View.resolveSize(baseContentSizePx, heightMeasureSpec)

        setMeasuredDimension(width, height)
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
            MeasureSpec.EXACTLY,
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
        val result: List<Triple<Float, String, String>> = charges
            .groupBy { it.category }
            .mapValues { it.value.sumOf { charge -> charge.amount } }
            .map { Triple(it.value.toFloat() * 360 / sumAmount, it.key, "${it.value * 100 / sumAmount}%") }
            .sortedByDescending { it.first }
            .take(10)

        sectors = result
        invalidate()

//        val result = mutableListOf<Pair<Float, String>>()
//        for ((category, charge) in categoryies.entries) {
//            result
//        }
    }
}