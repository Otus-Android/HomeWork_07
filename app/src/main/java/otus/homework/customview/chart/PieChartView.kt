package otus.homework.customview.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import otus.homework.customview.models.Expense
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Кастомная View для отображения круговой диаграммы (Pie Chart).
 * Позволяет визуализировать траты по категориям и обрабатывать клики по секторам.
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Список данных для отрисовки каждого сектора. */
    private var data: List<PieData> = emptyList()
    
    /** Общая сумма всех трат. */
    private var totalAmount: Int = 0

    /** Идентификатор выбранной категории. */
    private var selectedCategory: String? = null
    
    // --- Кешированные значения для отрисовки ---
    private var viewCenterX = 0f
    private var viewCenterY = 0f
    private var viewPadding = 0f
    private var viewHoleRadius = 0f
    private var currentTitleText = ""
    private var currentAmountText = ""
    private var currentPercentText = ""
    
    private val labelTotal by lazy { context.getString(R.string.pie_total) }
    private val amountFormat by lazy { context.getString(R.string.pie_amount_format) }

    private val defaultSizePx = (DEFAULT_SIZE_DP * resources.displayMetrics.density).toInt()
    private val selectionOffsetPx = SELECTION_OFFSET_DP * resources.displayMetrics.density
    // -------------------------------------------

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /** Кисть для отрисовки текста (%). */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    /** Кисть для отрисовки центрального заголовка (Всего/Категория). */
    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
    }

    /** Кисть для отрисовки центрального значения (Сумма). */
    private val centerValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    /** Прямоугольник, в который вписывается диаграмма. */
    private val rectF = RectF()
    private val selectedRectF = RectF()

    /** Список цветов для секторов, загружаемый из ресурсов. */
    private val colors = listOf(
        R.color.pie_color_1, R.color.pie_color_2, R.color.pie_color_3,
        R.color.pie_color_4, R.color.pie_color_5, R.color.pie_color_6,
        R.color.pie_color_7, R.color.pie_color_8, R.color.pie_color_9,
        R.color.pie_color_10, R.color.pie_color_11, R.color.pie_color_12
    ).map { ContextCompat.getColor(context, it) }

    /**
     * Устанавливает данные для отображения.
     * 
     * @param expenses Список объектов расхода [Expense].
     */
    fun setData(expenses: List<Expense>) {
        val categoryMap = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        totalAmount = categoryMap.values.sum()

        if (totalAmount <= 0) {
            data = emptyList()
            updateCenterTextCache()
            invalidate()
            return
        }

        var startAngle = 0f
        
        data = categoryMap.entries.mapIndexed { index, entry ->
            val sweepAngle = (entry.value.toFloat() / totalAmount) * FULL_CIRCLE_DEGREES
            val pieData = PieData(
                category = entry.key,
                amount = entry.value,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                color = colors[index % colors.size]
            )
            startAngle += sweepAngle
            pieData
        }
        updateCenterTextCache()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(widthSize, heightSize).let { if (it == 0) defaultSizePx else it }

        setMeasuredDimension(size, size)
        textPaint.textSize = size / PERCENT_TEXT_SIZE_RATIO
        centerTitlePaint.textSize = size / CENTER_TITLE_TEXT_SIZE_RATIO
        centerValuePaint.textSize = size / CENTER_VALUE_TEXT_SIZE_RATIO
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGeometryCache()
    }

    /** Обновляет расчеты геометрии, зависящие от размеров View. */
    private fun updateGeometryCache() {
        viewCenterX = width / 2f
        viewCenterY = height / 2f
        viewPadding = width * CHART_PADDING_RATIO
        
        rectF.set(viewPadding, viewPadding, width - viewPadding, height - viewPadding)
        selectedRectF.set(
            viewPadding - selectionOffsetPx, 
            viewPadding - selectionOffsetPx, 
            width - viewPadding + selectionOffsetPx, 
            height - viewPadding + selectionOffsetPx
        )
        viewHoleRadius = (width / 2f - viewPadding) * HOLE_RADIUS_RATIO
    }

    /** Обновляет текстовый кеш для центрального блока. */
    private fun updateCenterTextCache() {
        if (selectedCategory != null) {
            val selectedData = data.find { it.category == selectedCategory }
            val amount = selectedData?.amount ?: 0
            val percentage = if (totalAmount > 0) (amount.toFloat() / totalAmount * 100).toInt() else 0
            
            currentTitleText = selectedCategory!!
            currentAmountText = String.format(Locale.getDefault(), amountFormat, amount)
            currentPercentText = String.format(Locale.getDefault(), PERCENT_FORMAT_PARENTHESES, percentage)
        } else {
            currentTitleText = labelTotal
            currentAmountText = String.format(Locale.getDefault(), amountFormat, totalAmount)
            currentPercentText = ""
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        drawSectors(canvas)
        drawHole(canvas)
        drawCenterText(canvas)
    }

    /** Отрисовывает сектора диаграммы и текстовые проценты внутри них. */
    private fun drawSectors(canvas: Canvas) {
        // Измеряем ширину самого широкого возможного текста для расчета порога
        val minTextWidth = textPaint.measureText(MAX_PERCENT_LABEL)
        val textRadius = (viewCenterX - viewPadding) * PERCENT_TEXT_RADIUS_RATIO

        data.forEach { pieData ->
            paint.color = pieData.color
            
            val targetRect = if (pieData.category == selectedCategory) selectedRectF else rectF
            canvas.drawArc(targetRect, pieData.startAngle, pieData.sweepAngle, true, paint)

            // Вычисляем длину дуги на радиусе отрисовки текста s = r * theta(rad)
            val arcLength = textRadius * Math.toRadians(pieData.sweepAngle.toDouble()).toFloat()
            
            // Рисуем только если текст помещается с небольшим запасом
            if (arcLength > minTextWidth * TEXT_MIN_MARGIN_RATIO) {
                drawPercentage(canvas, pieData, textRadius)
            }
        }
    }

    /** Рисует процентное значение внутри конкретного сектора. */
    private fun drawPercentage(canvas: Canvas, pieData: PieData, radius: Float) {
        // Перевод медианного угла в радианы для sin/cos
        val medianAngleRad = Math.toRadians((pieData.startAngle + pieData.sweepAngle / 2f).toDouble())
        val x = (viewCenterX + radius * cos(medianAngleRad)).toFloat()
        val y = (viewCenterY + radius * sin(medianAngleRad)).toFloat()
        
        val percentage = if (totalAmount > 0) (pieData.amount.toFloat() / totalAmount * 100).toInt() else 0
        canvas.drawText(String.format(Locale.getDefault(), PERCENT_FORMAT, percentage), x, y + textPaint.textSize / 3f, textPaint)
    }

    /** Рисует центральный круг («дырку»), создавая эффект пончика. */
    private fun drawHole(canvas: Canvas) {
        paint.color = Color.WHITE
        canvas.drawCircle(viewCenterX, viewCenterY, viewHoleRadius, paint)
    }

    /** Рисует информационный текст (Заголовок, Сумму и Проценты) в центре диаграммы. */
    private fun drawCenterText(canvas: Canvas) {
        val hasPercent = currentPercentText.isNotEmpty()
        
        // Смещение заголовка вверх
        val titleY = if (hasPercent) {
            viewCenterY - centerTitlePaint.textSize * 1.2f
        } else {
            viewCenterY - centerTitlePaint.textSize / CENTER_TITLE_VERTICAL_OFFSET_RATIO
        }
        
        canvas.drawText(currentTitleText, viewCenterX, titleY, centerTitlePaint)

        // Сумма по центру (чуть ниже или ровно в центре)
        val amountY = if (hasPercent) viewCenterY + centerValuePaint.textSize / 2f else {
            viewCenterY + centerValuePaint.textSize * CENTER_VALUE_VERTICAL_OFFSET_RATIO
        }
        canvas.drawText(currentAmountText, viewCenterX, amountY, centerValuePaint)

        // Проценты на третьей строке
        if (hasPercent) {
            val percentY = amountY + centerTitlePaint.textSize * 1.2f
            canvas.drawText(currentPercentText, viewCenterX, percentY, centerTitlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x - viewCenterX
            val y = event.y - viewCenterY

            val distance = sqrt(x.pow(2) + y.pow(2))

            // Если клик в "дырке" - сбрасываем выделение
            if (distance < viewHoleRadius) {
                if (selectedCategory != null) {
                    selectedCategory = null
                    updateCenterTextCache()
                    invalidate()
                }
                return true
            }

            if (distance <= width / 2f) {
                var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
                if (angle < 0) angle += 360f

                data.find { angle >= it.startAngle && angle < (it.startAngle + it.sweepAngle) }?.let {
                    selectedCategory = if (selectedCategory == it.category) null else it.category
                    updateCenterTextCache()
                    performClick()
                    invalidate()
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartSavedState(superState, data, selectedCategory)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PieChartSavedState) {
            super.onRestoreInstanceState(state.superState)
            this.data = state.data
            this.selectedCategory = state.selectedCategory
            this.totalAmount = this.data.sumOf { it.amount }
            updateCenterTextCache()
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    companion object {
        private const val FULL_CIRCLE_DEGREES = 360f
        private const val DEFAULT_SIZE_DP = 150
        private const val CHART_PADDING_RATIO = 0.12f
        private const val SELECTION_OFFSET_DP = 8f
        private const val HOLE_RADIUS_RATIO = 0.55f
        private const val PERCENT_TEXT_RADIUS_RATIO = 0.75f
        
        private const val PERCENT_TEXT_SIZE_RATIO = 22f 
        private const val CENTER_TITLE_TEXT_SIZE_RATIO = 25f
        private const val CENTER_VALUE_TEXT_SIZE_RATIO = 15f
        
        private const val CENTER_TITLE_VERTICAL_OFFSET_RATIO = 4f
        private const val CENTER_VALUE_VERTICAL_OFFSET_RATIO = 0.8f

        private const val TEXT_MIN_MARGIN_RATIO = 1.3f

        private const val MAX_PERCENT_LABEL = "100%"
        private const val PERCENT_FORMAT = "%d%%"
        private const val PERCENT_FORMAT_PARENTHESES = "(%d%%)"
    }
}
