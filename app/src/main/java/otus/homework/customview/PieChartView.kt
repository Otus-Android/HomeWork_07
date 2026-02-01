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
import androidx.core.graphics.toColorInt
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Данные для графика
    private var categories: List<CategoryData> = emptyList()
    private var totalAmount: Double = 0.0

    // Стиль и отступы
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Цвета для категорий
    private val colors = listOf(
        "#FF6B6B".toColorInt(), // Красный
        "#4ECDC4".toColorInt(), // Бирюзовый
        "#FFD166".toColorInt(), // Желтый
        "#06D6A0".toColorInt(), // Зеленый
        "#118AB2".toColorInt(), // Синий
        "#073B4C".toColorInt(), // Темно-синий
        "#EF476F".toColorInt(), // Розовый
        "#26547C".toColorInt(), // Темно-голубой
        "#FFD166".toColorInt(), // Светло-желтый
        "#83D0CB".toColorInt(), // Светло-бирюзовый
        "#F78E69".toColorInt(), // Оранжевый
        "#5D576B".toColorInt()  // Фиолетовый
    )

    // Выбранная категория
    private var selectedCategory: CategoryData? = null
    private var selectedCategoryIndex: Int = -1

    // Коллбек для кликов
    var onCategoryClickListener: ((CategoryData) -> Unit)? = null

    // Геометрия
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var holeRadius = 0f
    private val padding = 20f
    private val selectedOffset = 15f

    private val rect = RectF()

    init {
        setupPaints()
    }

    private fun setupPaints() {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f

        selectedPaint.style = Paint.Style.FILL
        selectedPaint.color = Color.LTGRAY

        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        textPaint.textAlign = Paint.Align.CENTER

        centerPaint.color = Color.WHITE
        centerPaint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredSize = 400 // Минимальный желаемый размер

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize // MeasureSpec.UNSPECIFIED
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize // // MeasureSpec.UNSPECIFIED
        }

        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - padding
        holeRadius = radius * 0.4f // Внутренний радиус для кольца
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (categories.isEmpty()) return

        var startAngle = -90f

        // Рисуем сектора
        categories.forEachIndexed { index, category ->
            val sweepAngle = category.percentage * 360f / 100f

            // Выбираем цвет
            paint.color = colors[index % colors.size]

            // Определяем, выделен ли этот сектор
            val isSelected = selectedCategoryIndex == index
            val offset = if (isSelected) selectedOffset else 0f

            // Вычисляем угол для смещения
            val midAngle = startAngle + sweepAngle / 2
            val offsetX = offset * cos(Math.toRadians(midAngle.toDouble())).toFloat()
            val offsetY = offset * sin(Math.toRadians(midAngle.toDouble())).toFloat()

            // Рисуем сектор
            rect.set(
                centerX - radius + offsetX,
                centerY - radius + offsetY,
                centerX + radius + offsetX,
                centerY + radius + offsetY
            )

            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            // Если процент больше 5%, рисуем текст
            if (category.percentage > 5f) {
                val textAngle = startAngle + sweepAngle / 2
                val textRadius = radius * 0.7f
                val textX = centerX + offsetX + textRadius * cos(Math.toRadians(textAngle.toDouble())).toFloat()
                val textY = centerY + offsetY + textRadius * sin(Math.toRadians(textAngle.toDouble())).toFloat()

                val percentText = "${category.percentage.toInt()}%"
                canvas.drawText(percentText, textX, textY, textPaint)
            }

            startAngle += sweepAngle
        }

        // Рисуем центральный круг (делаем кольцо)
        canvas.drawCircle(centerX, centerY, holeRadius, centerPaint)

        // Рисуем информацию в центре
        if (selectedCategory != null) {
            drawCenterInfo(canvas, selectedCategory!!)
        } else {
            drawTotalInfo(canvas)
        }
    }

    private fun drawTotalInfo(canvas: Canvas) {
        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        infoPaint.color = Color.BLACK
        infoPaint.textSize = 32f
        infoPaint.textAlign = Paint.Align.CENTER

        val categoryPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        categoryPaint.color = Color.GRAY
        categoryPaint.textSize = 24f
        categoryPaint.textAlign = Paint.Align.CENTER

        val totalText = "Всего"
        val amountText = String.format(Locale.getDefault(),"%.2f ₽", totalAmount)

        canvas.drawText(totalText, centerX, centerY - 20, infoPaint)
        canvas.drawText(amountText, centerX, centerY + 30, infoPaint)
    }

    private fun drawCenterInfo(canvas: Canvas, category: CategoryData) {
        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        infoPaint.color = Color.BLACK
        infoPaint.textSize = 28f
        infoPaint.textAlign = Paint.Align.CENTER

        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        amountPaint.color = Color.GRAY
        amountPaint.textSize = 22f
        amountPaint.textAlign = Paint.Align.CENTER

        canvas.drawText(category.category, centerX, centerY - 30, infoPaint)
        canvas.drawText(
            "${category.percentage.toInt()}%",
            centerX,
            centerY + 10,
            amountPaint
        )
    }

    /**
     * Обработка кликов на сектора
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Проверяем, находится ли точка внутри круга
            val distanceFromCenter = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))

            if (distanceFromCenter >= holeRadius && distanceFromCenter <= radius) {
                // Вычисляем угол касания
                var angle = (Math.toDegrees(atan2((y - centerY).toDouble(),
                    (x - centerX).toDouble())) + 360) % 360

                // Корректируем угол (начало с -90 градусов)
                angle = (angle + 90) % 360

                // Находим сектор по углу
                var currentAngle = 0f
                categories.forEachIndexed { index, category ->
                    val sweepAngle = category.percentage * 360f / 100f

                    if (angle >= currentAngle && angle < currentAngle + sweepAngle) {
                        selectedCategory = category
                        selectedCategoryIndex = index
                        onCategoryClickListener?.invoke(category)
                        invalidate()
                        return true
                    }
                    currentAngle += sweepAngle
                }
            }

            // Сброс выделения при клике вне сектора
            selectedCategory = null
            selectedCategoryIndex = -1
            invalidate()
        }

        return super.onTouchEvent(event)
    }

    /**
     * Установка данных для отображения
     */
    fun setData(expenses: List<Expense>) {
        // Группируем по категориям
        val grouped = expenses.groupBy { it.category }

        categories = grouped.map { (category, expenses) ->
            val total = expenses.sumOf { it.amount }
            CategoryData(
                category = category,
                totalAmount = total,
                percentage = 0f,
                color = 0
            )
        }.sortedByDescending { it.totalAmount }

        totalAmount = categories.sumOf { it.totalAmount }

        // Вычисляем проценты
        categories = categories.map { category ->
            category.copy(
                percentage = (category.totalAmount / totalAmount * 100).toFloat()
            )
        }

        invalidate()
    }

    /**
     * Сохранение состояния View
     */
    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.selectedCategoryIndex = selectedCategoryIndex
        savedState.selectedCategoryName = selectedCategory?.category
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            selectedCategoryIndex = state.selectedCategoryIndex
            // Восстанавливаем категорию
            if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size) {
                selectedCategory = categories[selectedCategoryIndex]
            } else if (state.selectedCategoryName != null) {
                // Ищем категорию по имени
                setSelectedCategory(state.selectedCategoryName!!)
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /**
     * Получить выбранную категорию
     */
    fun getSelectedCategory(): CategoryData? {
        return if (selectedCategoryIndex >= 0 && selectedCategoryIndex < categories.size) {
            categories[selectedCategoryIndex]
        } else {
            null
        }
    }

    /**
     * Установить выбранную категорию
     */
    fun setSelectedCategory(categoryName: String) {
        val index = categories.indexOfFirst { it.category == categoryName }
        if (index >= 0) {
            selectedCategoryIndex = index
            selectedCategory = categories[index]
            invalidate()
        }
    }

}