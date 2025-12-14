package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.RawRes
import org.json.JSONArray
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    enum class ColorMode { MONO, COLOR }
    enum class TextMode { NONE, INNER, OUTER }
    enum class WidthMode { CONST, VAR }

    private data class Slice(
        val value: Float,
        val percent: Float,
    )

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = sp(12f)
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = sp(18f)
        isFakeBoldText = true
    }
    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = sp(14f)
    }

    private val arcBounds = RectF()

    private var colorMode: ColorMode = ColorMode.COLOR
    private var textMode: TextMode = TextMode.OUTER
    private var widthMode: WidthMode = WidthMode.CONST

    private var slices: List<Slice> = emptyList()
    private var totalAmount: Long = 0L

    private val palette = intArrayOf(
        0xFF8DD3C7.toInt(), 0xFFFFFFB3.toInt(), 0xFFBEBADA.toInt(), 0xFFFB8072.toInt(),
        0xFF80B1D3.toInt(), 0xFFFDB462.toInt(), 0xFFB3DE69.toInt(), 0xFFFCCDE5.toInt(),
        0xFFD9D9D9.toInt(), 0xFFBC80BD.toInt(), 0xFFCCEBC5.toInt(), 0xFFFFED6F.toInt()
    )

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PieChartView, defStyleAttr, 0)
            try {
                colorMode = when (a.getInt(R.styleable.PieChartView_colorMode, 1)) {
                    0 -> ColorMode.MONO
                    else -> ColorMode.COLOR
                }
                textMode = when (a.getInt(R.styleable.PieChartView_textMode, 2)) {
                    0 -> TextMode.NONE
                    1 -> TextMode.INNER
                    else -> TextMode.OUTER
                }
                widthMode = when (a.getInt(R.styleable.PieChartView_widthMode, 0)) {
                    1 -> WidthMode.VAR
                    else -> WidthMode.CONST
                }
            } finally {
                a.recycle()
            }
        }

        if (isInEditMode) {
            setSlices(
                listOf(
                    Slice(27.4f, 27.4f),
                    Slice(18.9f, 18.9f),
                    Slice(35.6f, 35.6f),
                    Slice(18.1f, 18.1f),
                ),
                total = 100,
            )
        } else {
            loadFromRaw(R.raw.payload)
        }
    }

    fun reload() {
        loadFromRaw(R.raw.payload)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = dp(240f)

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val maxW = if (wMode == MeasureSpec.UNSPECIFIED) desired else wSize
        val maxH = if (hMode == MeasureSpec.UNSPECIFIED) desired else hSize

        val size = min(maxW, maxH)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (slices.isEmpty() || totalAmount <= 0) {
            return
        }

        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = (width - paddingRight).toFloat()
        val contentBottom = (height - paddingBottom).toFloat()
        val contentW = (contentRight - contentLeft).coerceAtLeast(0f)
        val contentH = (contentBottom - contentTop).coerceAtLeast(0f)
        val diameter = min(contentW, contentH)

        val cx = contentLeft + contentW / 2f
        val cy = contentTop + contentH / 2f
        val outerLabelReserve = if (textMode == TextMode.OUTER) dp(16f).toFloat() + labelPaint.textSize else 0f

        val availableOuterEdgeRadius = (diameter / 2f - outerLabelReserve).coerceAtLeast(0f)
        val baseThickness = availableOuterEdgeRadius / 3f
        if (baseThickness <= 0f) return

        val ringCenterRadius = (availableOuterEdgeRadius - baseThickness / 2f).coerceAtLeast(0f)
        arcBounds.set(
            cx - ringCenterRadius,
            cy - ringCenterRadius,
            cx + ringCenterRadius,
            cy + ringCenterRadius,
        )

        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val thicknessMultiplier = if (widthMode == WidthMode.VAR && index % 2 == 1) 0.75f else 1f
            val thickness = baseThickness * thicknessMultiplier
            val halfStroke = thickness / 2f

            arcPaint.strokeWidth = thickness
            arcPaint.color = sliceColor(index, slices.size)
            val sweep = 360f * (slice.value / totalAmount.toFloat())
            canvas.drawArc(arcBounds, startAngle, sweep, false, arcPaint)

            if (textMode != TextMode.NONE) {
                val outerEdgeRadius = ringCenterRadius + halfStroke
                drawLabel(canvas, cx, cy, ringCenterRadius, outerEdgeRadius, startAngle, sweep, slice)
            }

            startAngle += sweep
        }

        drawCenterTotal(canvas, cx, cy)
    }

    private fun drawCenterTotal(canvas: Canvas, cx: Float, cy: Float) {
        val title = "Всего"
        val totalText = NumberFormat.getIntegerInstance().format(totalAmount)

        val titleFm = centerTitlePaint.fontMetrics
        val totalFm = centerPaint.fontMetrics
        val titleHeight = titleFm.descent - titleFm.ascent
        val totalHeight = totalFm.descent - totalFm.ascent
        val spacing = dp(4f).toFloat()
        val blockHeight = titleHeight + spacing + totalHeight
        val blockTop = cy - blockHeight / 2f

        val titleBaseline = blockTop - titleFm.ascent
        val totalBaseline = titleBaseline + titleHeight + spacing - totalFm.ascent

        canvas.drawText(title, cx, titleBaseline, centerTitlePaint)
        canvas.drawText(totalText, cx, totalBaseline, centerPaint)
    }

    private fun drawLabel(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        ringCenterRadius: Float,
        outerEdgeRadius: Float,
        startAngle: Float,
        sweepAngle: Float,
        slice: Slice,
    ) {
        val locale = Locale.getDefault()
        val percentText = String.format(locale, "%.1f", slice.percent)
        val text = "$percentText%"

        val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())

        val labelRadius = when (textMode) {
            TextMode.INNER -> ringCenterRadius
            TextMode.OUTER -> outerEdgeRadius + dp(16f).toFloat()
            TextMode.NONE -> return
        }

        val x = (cx + cos(midAngleRad) * labelRadius).toFloat()
        val y = (cy + sin(midAngleRad) * labelRadius).toFloat()

        val fm = labelPaint.fontMetrics
        canvas.drawText(text, x, y - (fm.ascent + fm.descent) / 2f, labelPaint)
    }

    private fun sliceColor(index: Int, sliceCount: Int): Int {
        return when (colorMode) {
            ColorMode.MONO -> monoColor(index, sliceCount)
            ColorMode.COLOR -> {
                val n = sliceCount.coerceAtLeast(1)
                val idx = floor((index + 0.5) * palette.size.toDouble() / n.toDouble()).toInt()
                palette[idx.coerceIn(0, palette.lastIndex)]
            }
        }
    }

    private fun monoColor(index: Int, sliceCount: Int): Int {
        val n = sliceCount.coerceAtLeast(1)
        val evenCount = (n + 1) / 2
        val pos = if (index % 2 == 0) index / 2 else evenCount + (index - 1) / 2

        val start = 0.95
        val end = 0.05
        val t = if (n == 1) 0.0 else pos.toDouble() / (n - 1).toDouble()
        val level = (start + (end - start) * t).coerceIn(end, start)
        val gray = (255.0 * level).roundToInt().coerceIn(0, 255)
        return Color.rgb(gray, gray, gray)
    }

    private fun loadFromRaw(@RawRes rawRes: Int) {
        val json = resources.openRawResource(rawRes).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val byCategory = linkedMapOf<String, Long>()
        var total = 0L

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val category = obj.optString("category", "")
            val amount = obj.optLong("amount", 0L)
            if (category.isBlank() || amount <= 0L) continue
            byCategory[category] = (byCategory[category] ?: 0L) + amount
            total += amount
        }

        val sliceList = byCategory
            .entries
            .sortedByDescending { it.value }
            .map { (_, amount) ->
                val percent = if (total > 0L) amount.toDouble() * 100.0 / total.toDouble() else 0.0
                Slice(amount.toFloat(), percent.toFloat())
            }

        setSlices(sliceList, total)
    }

    private fun setSlices(slices: List<Slice>, total: Long) {
        this.slices = slices
        this.totalAmount = total
        invalidate()
    }

    private fun dp(value: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics,
        ).toInt()
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            resources.displayMetrics,
        )
    }
}
