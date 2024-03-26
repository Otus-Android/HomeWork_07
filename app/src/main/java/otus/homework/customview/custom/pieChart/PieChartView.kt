package otus.homework.customview.custom.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.custom.ChartData
import otus.homework.customview.custom.getRandomColor
import kotlin.math.PI

class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val KEY_SELECTED_INDEX = "selected_index"
    private val KEY_DATA = "current_data"
    private val KEY_COLOR_LIST = "color_list"

    private var data: List<ChartData> = emptyList()
    private var listCategory: List<Category> = emptyList()
    private lateinit var circleCenter: Position
    private var circleRadius = 0
    private var totalAmount = 1
    private var selectedSectorIndex = -1
    private val colors = mutableListOf<Int>()

    init {
        for (i in 0..15) {
            colors.add(getRandomColor())
        }
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var clickListener: ClickListenerPieChart? = null

    fun setData(data: List<ChartData>) {
        this.data = data
        if (data.isEmpty()) return
        this.totalAmount = data.sumOf { it.amount }
        if (totalAmount == 0) totalAmount = 1
        var startAngle = 0f
        data.groupBy { pie -> pie.category }.forEach { pairData ->
            val endAngle = ((pairData.value.sumOf { it.amount }) * 360) / totalAmount.toFloat()
            val category = Category(pairData.key, CategoryAngle(startAngle, startAngle + endAngle))
            startAngle += endAngle
            listCategory += category
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = width / 2f
        val centerY = height / 2f
        circleCenter = Position(centerX, centerY)
        circleRadius = centerX.coerceAtMost(centerY).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (listCategory.isEmpty()) return

        rect.set(
            circleCenter.x - circleRadius,
            circleCenter.y - circleRadius,
            circleCenter.x + circleRadius,
            circleCenter.y + circleRadius
        )

        listCategory.forEachIndexed { index, category ->
            paint.color = colors[index]
            canvas.drawArc(
                rect,
                category.angle.startAngle,
                category.angle.endAngle - category.angle.startAngle,
                true,
                paint
            )
             if (index == selectedSectorIndex) {
                 canvas.drawArc(
                     rect,
                     category.angle.startAngle,
                     category.angle.endAngle - category.angle.startAngle,
                     true,
                     highlightPaint
                 )
             }
        }
    }

    private fun <T : Comparable<T>> T.isInRange(min: T, max: T): Boolean {
        return if (max > min)
            this in min..max
        else
            this in max..min
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchPosition = Position(event.x, event.y)
        val category = getCategoryFromPosition(touchPosition)
        if (category >= 0) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    clickListener?.click(listCategory[category].name)
                }
            }
            setSelectedSector(category)
        }
        return true
    }

    private fun Position.toAngle(): Float {
        fun Float.toDegrees(): Double {
            return this * 180 / PI
        }

        // Вычисляем угол относительно центра окружности
        val angleRad = kotlin.math.atan2(
            y - circleCenter.y,
            x - circleCenter.x
        )

        // Преобразуем угол из радиан в градусы
        val angleDeg = angleRad.toDegrees().toFloat()

        // Приводим угол к диапазону от 0 до 360 градусов

        return if (angleDeg < 0) angleDeg + 360 else angleDeg
    }

    // Функция для определения сектора, к которому принадлежит точка
    private fun getCategoryFromPosition(touchPosition: Position): Int {
        // Находим вектор от центра окружности к точке
        val pointAngle = touchPosition.toAngle()

        // Проходим по каждому сектору
        listCategory.forEachIndexed { index, category ->
            // Если знак векторного произведения положителен, то точка находится в секторе
            if (pointAngle.isInRange(category.angle.startAngle, category.angle.endAngle)) {
                return index
            }
        }
        // Если точка не принадлежит ни одному из секторов, возвращаем -1
        return -1
    }

    private fun setSelectedSector(index: Int) {
        selectedSectorIndex = index
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putInt(KEY_SELECTED_INDEX, selectedSectorIndex)
            putIntegerArrayList(KEY_COLOR_LIST, ArrayList(colors))
            putParcelableArray(KEY_DATA, listCategory.toTypedArray())
            putParcelable("superState", superState)
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var savedState = state
        if (savedState is Bundle) {
            val parcelableArray = savedState.getParcelableArray(KEY_DATA)
            listCategory = parcelableArray?.map { it as Category } ?: listCategory
            setSelectedSector(savedState.getInt(KEY_SELECTED_INDEX))
            colors.clear()
            savedState.getIntegerArrayList(KEY_COLOR_LIST)?.toMutableList()?.let { colors.addAll(it) }
            savedState = savedState.getParcelable("superState")
        }
        super.onRestoreInstanceState(savedState)
    }

    fun setClickListener(clickListenerPieChart: ClickListenerPieChart) {
        this.clickListener = clickListenerPieChart
    }

}