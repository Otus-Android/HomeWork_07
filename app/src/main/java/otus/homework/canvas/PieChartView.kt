package otus.homework.canvas

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt


class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var myBackgroundColor: Int = Color.WHITE
    private var gap = 0F
    private var defaultWidth = 25F
    private var startAngle = -90F
    private var padding = 20F
    private var numPressed: Int? = null

    private var pieChartSectorDataArray: List<PieChartSectorData>? = null
    private var callback: ((category: String, Int) -> Unit)? = null
    private var summaryAngle: Float = 0F
    private var minDiameter: Float = 0F
    private var maxDiameter: Float = 0F

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)
        myBackgroundColor = typedArray.getColor(R.styleable.PieChartView_backgroundColor, myBackgroundColor)
        gap = typedArray.getDimension(R.styleable.PieChartView_defaultGap, gap)
        defaultWidth = typedArray.getDimension(R.styleable.PieChartView_defaultWidth, defaultWidth)
        startAngle = typedArray.getDimension(R.styleable.PieChartView_startAngle, startAngle)
        padding = typedArray.getDimension(R.styleable.PieChartView_padding, padding)
        typedArray.recycle()
    }

    fun Int.modToString() = when(this) {
        MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        MeasureSpec.EXACTLY->"EXACTLY"
        MeasureSpec.AT_MOST->"AT_MOST"
        else -> "---"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMod = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMod = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        Log.d("***[", "widthMod=${widthMod.modToString()} widthSize=$widthSize heightMod=${heightMod.modToString()} heightSize=$heightSize")
        val size = minOf(widthSize,heightSize)
        setMeasuredDimension(size,size)
    }

    fun getNumSector(x: Float, y: Float): Int? {
        val diameter: Float = sqrt(x*x + y*y) * 2
        //Log.d("***[", "***2 radius=$radius minRadius=${reCalcSize(minDiameter)} maxRadius=${reCalcSize(maxDiameter)}")
        if (diameter < reCalcSize(minDiameter) || diameter > reCalcSize(maxDiameter)) {
            return null
        }
        var angle = atan2(y, x) / (2 * PI) * 360
        angle = if (angle < 0) angle + 360 else angle

        for (num: Int in 0 until pieChartSectorDataArray!!.size) {
            val sector = pieChartSectorDataArray!![num]
            var startAngle = sector.startAngle
            var endAngle = sector.startAngle + sector.angle
            if (startAngle < 0 && endAngle < 0) {
                startAngle += 360
                endAngle += 360
            }
            //Log.d("***[", "angle=$angle startAngle=${startAngle} endAngle=${endAngle}")
            if (angle > startAngle && angle < endAngle ||
                startAngle < 0 && endAngle > 0 && angle > startAngle + 360) {
                return num
            }
        }
        return null
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (pieChartSectorDataArray == null) {
            return false
        }
        numPressed =
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val displaySize = minOf(width, height).toFloat()
                    val num = getNumSector(event.x - displaySize / 2, event.y - displaySize / 2)
                    num?.let { callback?.invoke(pieChartSectorDataArray!![it].category, it) }
                    num
                }
                MotionEvent.ACTION_UP -> null
                else -> return false
            }
        return true
    }

    fun setData(
        pieChartSectorDataArray: List<PieChartSectorData>?,
        minRadius: Float,
        callback: (category: String, Int) -> Unit
    ): Boolean {
        invalidate()
        this.pieChartSectorDataArray = pieChartSectorDataArray
        pieChartSectorDataArray ?: return false
        this.callback = callback
        this.minDiameter = minRadius
        // set pre value
        var calcMinRadius = Float.MAX_VALUE
        summaryAngle = 0F
        maxDiameter = 0F
        var numColor = 0
        val presetColors: IntArray = context.resources.getIntArray(R.array.presetColors)
        // Find max & min
        for (sector in pieChartSectorDataArray) {
            calcMinRadius = minOf(calcMinRadius, sector.radius)
            summaryAngle += sector.angle
            maxDiameter = maxOf(maxDiameter, sector.radius)
            if (sector.color == Int.MAX_VALUE) {
                sector.color = presetColors[numColor++]
            }
        }
        if (minRadius < 0) {
            this.minDiameter = calcMinRadius * 3 / 4
        } else if (calcMinRadius < minRadius) {
            this.pieChartSectorDataArray = null
            return false
        }
        //
        val k = (360 - gap * pieChartSectorDataArray.size) / summaryAngle
        var curtrentAngle = startAngle
        for (sector in pieChartSectorDataArray) {
            sector.angle *= k
            sector.startAngle = curtrentAngle
            curtrentAngle += sector.angle + gap
        }
        return true
    }

    private fun reCalcSize(size: Float) = size * (minOf(width,height).toFloat() - padding * 2) / maxDiameter

    private fun getRect(diameter: Float): RectF {
        val displaySize = minOf(width,height).toFloat()
        val calcPadding = (displaySize - reCalcSize(diameter)) / 2
        return RectF(calcPadding, calcPadding, displaySize-calcPadding, displaySize-calcPadding)
    }

    val path = Path()
    val paint = Paint().apply {
        textSize = 30F
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    override fun onDrawForeground(canvas: Canvas) {

        canvas.apply {
            drawColor(myBackgroundColor)

            if (pieChartSectorDataArray == null) {
                return
            }

            for (sector in pieChartSectorDataArray!!) {
                DrawSector(sector)
            }

            paint.color = myBackgroundColor
            drawArc(getRect(minDiameter), 0F, 360F, true, paint)
        }
    }

    fun Canvas.DrawSector(sector: PieChartSectorData) {
        val rect = getRect(sector.radius)
        paint.color = sector.color
        drawArc(rect, sector.startAngle, sector.angle, true, paint)

        if (sector.text != null) {
            val textRect = Rect()
            paint.getTextBounds(sector.text, 0, sector.text.length, textRect)
            val height = textRect.height()

            val calcHeight = sector.angle * rect.width() / PI / 100 // TO?DO?changed
            if (height < calcHeight) {
                paint.color = Color.BLACK
                path.reset();
                path.addArc(rect, sector.startAngle, sector.angle)
                drawTextOnPath(sector.text, path, 0F, 0F, paint)
            }
        }
    }
}