package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RawRes
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import kotlin.math.*
import org.json.JSONArray

class ViraPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        private const val ERROR_DATA_SOURCE_NOT_SET =
            "ViraPieChartView: dataSource is not set. " +
                    "Provide app:dataSource=\"@raw/...\" in XML or call setDataSource(@RawRes) from Activity/Fragment."
        
        // Кешируемые raw ресурсы для ускорения
        private val rawResourceCache = mutableMapOf<String, Int>()
    }

    enum class ColorMode { MONO, COLOR }
    enum class TextMode { NONE, INNER, OUTER }
    enum class WidthMode { CONST, VAR }

    data class Entry(
        val name: String,
        val category: String,
        val time: Long,
        val amount: Long,
    )

    fun interface OnSectorClickListener {
        fun onSectorClick(category: String)
    }

    private data class Slice(
        val name: String,
        val category: String,
        val time: Long,
        val amount: Long,
        val percent: Float,
        // Геометрическая информация для кликов (заполняется в onDraw)
        var startAngle: Float = 0f,
        var sweepAngle: Float = 0f,
        var innerRadius: Float = 0f,
        var outerRadius: Float = 0f,
        var centerX: Float = 0f,
        var centerY: Float = 0f,
    )

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = ViewUtils.sp(context, 12f)
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = ViewUtils.sp(context, 18f)
        isFakeBoldText = true
    }
    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = ViewUtils.sp(context, 14f)
    }

    private val arcBounds = RectF()

    private var colorMode: ColorMode = ColorMode.COLOR
    private var textMode: TextMode = TextMode.OUTER
    private var widthMode: WidthMode = WidthMode.CONST

    private var titleText: String = "Title"
    private var dataSourceResId: Int = 0

    private var slices: List<Slice> = emptyList()
    private var totalAmount: Long = 0L

    private var onSectorClickListener: OnSectorClickListener? = null
    private var pressedSlice: Slice? = null

    // Кешируемые измерения для производительности
    private var cachedOuterLabelReserve: Float = 0f

    // Кешируемые font metrics для отрисовки текста
    private var labelFontHeight: Float = 0f
    private var centerTitleFontHeight: Float = 0f
    private var centerFontHeight: Float = 0f
    
    private val palette = intArrayOf(
        0xFF8DD3C7.toInt(), 0xFFFFFFB3.toInt(), 0xFFBEBADA.toInt(), 0xFFFB8072.toInt(),
        0xFF80B1D3.toInt(), 0xFFFDB462.toInt(), 0xFFB3DE69.toInt(), 0xFFFCCDE5.toInt(),
        0xFFD9D9D9.toInt(), 0xFFBC80BD.toInt(), 0xFFCCEBC5.toInt(), 0xFFFFED6F.toInt()
    )

    private fun updateCacheValues() {
        cachedOuterLabelReserve = if (textMode == TextMode.OUTER) {
            ViewUtils.dp(context, 16f).toFloat() + labelPaint.textSize
        } else {
            0f
        }

        labelFontHeight = labelPaint.fontMetrics.run { descent - ascent }
        centerTitleFontHeight = centerTitlePaint.fontMetrics.run { descent - ascent }
        centerFontHeight = centerPaint.fontMetrics.run { descent - ascent }
    }

    init {
        isSaveEnabled = true
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ViraPieChartView, defStyleAttr, 0)
            try {
                colorMode = when (a.getInt(R.styleable.ViraPieChartView_colorMode, 1)) {
                    0 -> ColorMode.MONO
                    else -> ColorMode.COLOR
                }
                textMode = when (a.getInt(R.styleable.ViraPieChartView_textMode, 2)) {
                    0 -> TextMode.NONE
                    1 -> TextMode.INNER
                    else -> TextMode.OUTER
                }
                widthMode = when (a.getInt(R.styleable.ViraPieChartView_widthMode, 0)) {
                    1 -> WidthMode.VAR
                    else -> WidthMode.CONST
                }

                titleText = a.getString(R.styleable.ViraPieChartView_titleText) ?: titleText

                dataSourceResId = a.getResourceId(R.styleable.ViraPieChartView_dataSource, 0)
                    .takeIf { it != 0 }
                    ?: resolveRawResId(a.getString(R.styleable.ViraPieChartView_dataSource))
                            ?: 0
            } finally {
                a.recycle()
            }
        }

        if (isInEditMode) {
            val (previewSlices, previewTotal) = buildPreviewData()
            setSlices(previewSlices, previewTotal)
        } else {
            if (dataSourceResId != 0) {
                setDataSource(dataSourceResId)
            }
        }

        updateCacheValues()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> wSize
            else -> wSize  // UNSPECIFIED - используем размер окна
        }

        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> {
                val squareHeight = width - paddingLeft - paddingRight
                min(squareHeight.coerceAtLeast(0), hSize)
            }

            else -> width - paddingLeft - paddingRight
        }

        setMeasuredDimension(width, height)
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
        val outerLabelReserve = cachedOuterLabelReserve

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

        slices.forEach {
            it.apply {
                startAngle = 0f
                sweepAngle = 0f
                innerRadius = 0f
                outerRadius = 0f
                centerX = 0f
                centerY = 0f
            }
        }

        var startAngle = -90f
        slices = slices.mapIndexed { index, slice ->
            val thicknessMultiplier = if (widthMode == WidthMode.VAR && index % 2 == 1) 0.75f else 1f
            val thickness = baseThickness * thicknessMultiplier
            val halfStroke = thickness / 2f

            arcPaint.strokeWidth = thickness
            arcPaint.color = sliceColor(index, slices.size)
            val sweep = 360f * (slice.amount.toFloat() / totalAmount.toFloat())
            canvas.drawArc(arcBounds, startAngle, sweep, false, arcPaint)

            slice.copy(
                startAngle = startAngle,
                sweepAngle = sweep,
                innerRadius = ringCenterRadius - halfStroke,
                outerRadius = ringCenterRadius + halfStroke,
                centerX = cx,
                centerY = cy
            ).also { updatedSlice ->
                if (textMode != TextMode.NONE) {
                    val outerEdgeRadius = ringCenterRadius + halfStroke
                    drawLabel(canvas, cx, cy, ringCenterRadius, outerEdgeRadius, startAngle, sweep, updatedSlice)
                }
                startAngle += sweep
            }
        }

        drawCenterTotal(canvas, cx, cy)
    }

    private fun drawCenterTotal(canvas: Canvas, cx: Float, cy: Float) {
        val title = titleText
        val totalText = NumberFormat.getIntegerInstance().format(totalAmount)

        val spacing = ViewUtils.dp(context, 4f)
        val blockHeight = centerTitleFontHeight + spacing + centerFontHeight
        val blockTop = cy - blockHeight / 2f

        val titleBaseline = blockTop - centerTitlePaint.fontMetrics.ascent
        val totalBaseline = titleBaseline + centerTitleFontHeight + spacing - centerPaint.fontMetrics.ascent

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
        val percentText = "%.1f%%".format(slice.percent)

        val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())

        val labelRadius = when (textMode) {
            TextMode.INNER -> ringCenterRadius
            TextMode.OUTER -> outerEdgeRadius + ViewUtils.dp(context, 16f).toFloat()
            TextMode.NONE -> return
        }

        val x = (cx + cos(midAngleRad) * labelRadius).toFloat()
        val y = (cy + sin(midAngleRad) * labelRadius).toFloat()

        canvas.drawText(percentText, x, y - (labelPaint.fontMetrics.ascent + labelPaint.fontMetrics.descent) / 2f, labelPaint)
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

    private fun loadEntriesFromRaw(@RawRes rawRes: Int): List<Entry> {
        val json = resources.openRawResource(rawRes).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val parsed = ArrayList<Entry>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val amount = obj.optLong("amount", 0L)
            if (amount <= 0L) continue
            parsed.add(
                Entry(
                    name = obj.optString("name", ""),
                    category = obj.optString("category", ""),
                    time = obj.optLong("time", 0L),
                    amount = amount,
                )
            )
        }
        return parsed
    }

    private fun buildPreviewData(): Pair<List<Slice>, Long> {
        val items = listOf(
            Slice(name = "A", category = "C1", time = 0L, amount = 274, percent = 0f),
            Slice(name = "B", category = "C2", time = 0L, amount = 189, percent = 0f),
            Slice(name = "C", category = "C3", time = 0L, amount = 356, percent = 0f),
            Slice(name = "D", category = "C4", time = 0L, amount = 181, percent = 0f),
        )
        val total = items.sumOf { it.amount }
        return items.withPercent(total) to total
    }

    private fun resolveRawResId(rawName: String?): Int? {
        val cleaned = rawName
            ?.trim()
            ?.removePrefix("@raw/")
            ?.removeSuffix(".json")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        // Проверяем кеш сначала
        rawResourceCache[cleaned]?.let { return it }

        // Вычисляем и кешируем результат
        return try {
            // Быстрая компиляция-time проверка через рефлексию
            val clazz = R.raw::class.java
            val field = clazz.getDeclaredField(cleaned)
            val resId = field.getInt(null)
            rawResourceCache[cleaned] = resId
            resId
        } catch (ex: Exception) {
            // Fallback на стандартный метод (только если ресурс не найден)
            val resId = context.resources.getIdentifier(cleaned, "raw", context.packageName)
            if (resId != 0) {
                rawResourceCache[cleaned] = resId
                resId
            } else null
        }
    }

    private fun List<Slice>.withPercent(total: Long): List<Slice> {
        if (total <= 0L) return this
        return map { slice ->
            val percent = slice.amount.toDouble() * 100.0 / total.toDouble()
            slice.copy(percent = percent.toFloat())
        }
    }

    private fun setSlices(slices: List<Slice>, total: Long) {
        this.slices = slices
        this.totalAmount = total
        invalidate()
    }

    fun setDataSource(@RawRes rawRes: Int) {
        if (rawRes == 0) {
            throw IllegalArgumentException(ERROR_DATA_SOURCE_NOT_SET)
        }
        dataSourceResId = rawRes
        setDataInternal(loadEntriesFromRaw(rawRes), keepDataSourceResId = rawRes)
    }

    fun setData(entries: List<Entry>) {
        dataSourceResId = 0
        setDataInternal(entries, keepDataSourceResId = 0)
    }

    private fun setDataInternal(entries: List<Entry>, keepDataSourceResId: Int) {
        dataSourceResId = keepDataSourceResId

        val filtered = entries.filter { it.amount > 0L }
        val total = filtered.sumOf { it.amount }
        if (total <= 0L) {
            setSlices(emptyList(), 0L)
            return
        }

        val sliceList = filtered
            .sortedByDescending { it.amount }
            .map {
                Slice(
                    name = it.name,
                    category = it.category,
                    time = it.time,
                    amount = it.amount,
                    percent = 0f,
                )
            }
            .withPercent(total)

        setSlices(sliceList, total)
    }

    fun setOnSectorClickListener(listener: OnSectorClickListener?) {
        onSectorClickListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                for (slice in slices) {
                    if (isPointInSector(x, y, slice)) {
                        pressedSlice = slice
                        return true
                    }
                }
                pressedSlice = null
                return false
            }

            MotionEvent.ACTION_UP -> {
                val slice = pressedSlice
                pressedSlice = null

                if (slice != null && isPointInSector(event.x, event.y, slice)) {
                    performClick()
                    onSectorClickListener?.onSectorClick(slice.category)
                    return true
                }
                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedSlice = null
                return false
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun isPointInSector(x: Float, y: Float, slice: Slice): Boolean {
        val dx = x - slice.centerX
        val dy = y - slice.centerY
        val distance = sqrt(dx.pow(2) + dy.pow(2))

        if (distance < slice.innerRadius || distance > slice.outerRadius) {
            return false
        }

        val angleRad = atan2(dy, dx)
        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
        if (angleDeg < 0) angleDeg += 360f

        var adjustedAngle = angleDeg + 90f
        if (adjustedAngle >= 360f) adjustedAngle -= 360f

        val startAngle = slice.startAngle + 90f
        val endAngle = startAngle + slice.sweepAngle

        return if (startAngle <= endAngle) {
            adjustedAngle in startAngle..endAngle
        } else {
            adjustedAngle >= startAngle || adjustedAngle <= endAngle
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return PieSavedState(
            superState = super.onSaveInstanceState(),
            colorModeName = colorMode.name,
            textModeName = textMode.name,
            widthModeName = widthMode.name,
            titleText = titleText,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is PieSavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        colorMode = state.colorModeName?.let { runCatching { ColorMode.valueOf(it) }.getOrNull() } ?: colorMode
        textMode = state.textModeName?.let { runCatching { TextMode.valueOf(it) }.getOrNull() } ?: textMode
        widthMode = state.widthModeName?.let { runCatching { WidthMode.valueOf(it) }.getOrNull() } ?: widthMode
        titleText = state.titleText ?: titleText

        updateCacheValues()

        // Данные восстанавливаются снаружи (Activity/VM) через setData(...)
        invalidate()
    }

    @Parcelize
    internal data class PieSavedState(
        val superState: Parcelable?,
        val colorModeName: String?,
        val textModeName: String?,
        val widthModeName: String?,
        val titleText: String?,
    ) : Parcelable

}
