package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.view.GestureDetectorCompat
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "PieChart"
private const val DEBUG_TAG = TAG

private const val CHART_THICKNESS_DP = 60f
private const val CHART_DIAMETER = 100F

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private var detector: GestureDetectorCompat

    private var minChartSizePx: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        CHART_DIAMETER + CHART_THICKNESS_DP,
        resources.displayMetrics
    ).roundToInt()

    private var thicknessPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        CHART_THICKNESS_DP,
        resources.displayMetrics
    )

    private var halfThicknessPx = thicknessPx / 2

    private var legendSizePx: Int = 0

    private var gesturePoint = PointF(-1f, -1f)

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

    private var selectListener: OnSelectListener? = null

    private val chartRect = RectF(halfThicknessPx, halfThicknessPx, diameterPx - thicknessPx, diameterPx - thicknessPx)
    private var chartSizePx = minChartSizePx

    private var legendLineHeight: Float
    private var percentHalfWidth: Float
    private var percentHalfHeight: Float

    private val gestureCallback = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            Log.d(DEBUG_TAG, "onSingleTapUp: $event")

            val height = chartRect.bottom + halfThicknessPx
            if (event.y > height) return true

            val radius = height / 2
            val x = event.x - chartRect.centerX()
            val y = event.y - chartRect.centerY()
            if (x * x + y * y > radius * radius) return true

            gesturePoint.set(event.x, event.y)
            invalidate()

            var rad = atan2(y, x)
            if (y < 0) {
                rad += (PI * 2).toFloat()
            }
            val angle = 180 * rad / PI

            Log.d(TAG, "rad: $rad, angle: $angle")

            var tempAngle = angle
            for (sector in sectors) {
                if (tempAngle < sector.angle) {
                    Log.d(TAG, "sector: $sector")
                    selectListener?.onSelect(this@PieChart, sector.category)
                    break
                }
                tempAngle -= sector.angle
            }

            return true
        }

        override fun onDown(event: MotionEvent): Boolean {
            Log.d(DEBUG_TAG, "onDown: $event")
            return true
        }
    }

    init {
        // Load attributes
        context.obtainStyledAttributes(
            attrs, R.styleable.PieChart, defStyleAttr, 0
        ).apply {
            this.recycle()
        }

        val fontMetrics = textPaint.fontMetrics
        legendLineHeight = fontMetrics.descent - fontMetrics.ascent
        percentHalfWidth = textPaint.measureText("100%") / 2
        percentHalfHeight = legendLineHeight / 2

        detector = GestureDetectorCompat(context, gestureCallback)
    }

    val gesturePaint = Paint().apply {
        this.color = Color.WHITE;
        this.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4f, resources.displayMetrics);
        this.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

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

        if (gesturePoint.x >= 0) {
//            textPaint.color = Color.BLACK
            canvas.drawPoint(gesturePoint.x, gesturePoint.y, gesturePaint)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "called onMeasure()")

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int
        if (wMode == MeasureSpec.EXACTLY && hSize - legendSizePx > wSize) {
            chartRect.right = wSize - halfThicknessPx
            chartRect.bottom = wSize - halfThicknessPx
            chartSizePx = wSize
            width = wSize
            height = View.resolveSize(legendSizePx + wSize, heightMeasureSpec)
        } else {
            width = View.resolveSize(minChartSizePx, widthMeasureSpec)
            height = View.resolveSize(minChartSizePx + legendSizePx, heightMeasureSpec)
            chartSizePx = minChartSizePx
            chartRect.right = diameterPx + halfThicknessPx
            chartRect.bottom = diameterPx + halfThicknessPx
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        Log.d(TAG, "called onSizeChanged()")

        calculatePercentAngles()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d(TAG, "called onLayout()")
    }

    private fun calculateDimension(mode: Int, size: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(minChartSizePx, size)
            MeasureSpec.UNSPECIFIED -> minChartSizePx
            else -> size
        }
    }

    fun setOnSelectListener(selectListener: OnSelectListener?) {
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

        calculatePercentAngles()

        legendSizePx = ceil(legendLineHeight * result.size).toInt()

        val isSizeChange = sectors.size != result.size
        sectors = result

        if (isSizeChange) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    private fun calculatePercentAngles() {
        var fullAngle = 0.0
        for (sector in sectors) {
            val textHalfWidth = textPaint.measureText(sector.percentText) / 2
            val radian = (fullAngle + sector.angle / 2) / 180.0 * PI
            sector.percentX = (chartRect.centerX() + cos(radian) * (chartRect.width() / 2) - textHalfWidth).toFloat()
            sector.percentY = (chartRect.centerY() + sin(radian) * (chartRect.height() / 2) + percentHalfHeight).toFloat()
            fullAngle += sector.angle
        }
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (detector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    fun interface OnSelectListener {
        fun onSelect(view: PieChart, category: String)
    }
}