package otus.homework.customview.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.times
import otus.homework.customview.utils.dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val STATE_SUPER = "superState"
        private const val STATE_COLORS = "colors"
        private const val STATE_SELECTED_ID = "selectedId"
    }

    // желаемый размер диаграммы
    private val defaultSize = 240.dp.toInt()

    // фактор уменьшения невыбранных секторов диаграммы
    private val zoomFactor = 0.05f

    // данные для чарта
    private var data: List<ChartData>? = null

    // углы секторов для отслеживания тапов
    private val startAngles = mutableListOf<Float>()
    private val endAngles = mutableListOf<Float>()

    // весь круг - 100%
    private var totalSum: Int = 0

    // цвета для секторов
    private var colors = IntArray(20) {
        // тёплые
        Color.rgb(
            Random.nextInt(15, 25) * 10,
            Random.nextInt(10, 25) * 10,
            Random.nextInt(0, 15) * 10
        )
    }

    private var selectedId: Int? = null

    private val piePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val pieRect = RectF()
    private val pieSelectedRect = RectF()

    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean = data?.let { list ->
            // радиус круга
            val r = pieSelectedRect.width() / 2
            // смещения от центра круга
            val dx = e.x - pieSelectedRect.centerX()
            val dy = e.y - pieSelectedRect.centerY()
            // расстояние от центра круга
            val dCenter = sqrt((dx * dx + dy * dy).toDouble())
            // определить сектор тапа, если внутри круга
            if (dCenter <= r) {
                // угол точки тапа
                val angle = Math.toDegrees(
                    atan2(dy.toDouble(), dx.toDouble())
                ).let {
                    if (it < 0.0) it + 360.0 else it
                }
                // найти сектор, в который попадает этот угол
                selectedId = getDataByAngle(angle)?.id
                // отработать изменения
                saySelected()
                invalidate()
            }
            true
        } ?: false

    })


    private fun getDataByAngle(angle: Double): ChartData? = data?.let { list ->
        var result: ChartData? = null
        var start: Float
        var end: Float
        for (i in startAngles.indices) {
            start = startAngles[i]
            end = endAngles[i]
            if (start <= end) {
                if (angle in start..end) {
                    result = list[i]
                    break
                }
            } else {
                // сектор пересекает 0 градусов
                if (angle >= start || angle <= end) {
                    result = list[i]
                    break
                }
            }
        }
        result
    }

    private fun getColorByIndex(index: Int) = colors[index % colors.size]

    private fun getColorById(id: Int) = data?.indexOfFirst {
        it.id == id
    }?.let {
        getColorByIndex(it)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (wMode, wSize) = widthMeasureSpec.let {
            MeasureSpec.getMode(it) to MeasureSpec.getSize(it)
        }
        val (hMode, hSize) = heightMeasureSpec.let {
            MeasureSpec.getMode(it) to MeasureSpec.getSize(it)
        }

        // для UNSPECIFIED - минимальный размер
        val w = when (wMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> wSize
            else -> defaultSize
        }

        // для UNSPECIFIED - минимальный размер
        val h = when (hMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> hSize
            else -> defaultSize
        }

        // всегда квадрат для зоны чарта
        val size = min(w, h).toFloat()
        // rect для выбранного сектора
        pieSelectedRect.set(0f, 0f, size, size)
        // остальные уменьшены на 5%
        pieRect.set(pieSelectedRect.times(1f - zoomFactor))

        setMeasuredDimension(w, h)
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)

        // размещение rect выбранной зоны
        val newLeft = (r - l - pieSelectedRect.width()) / 2
        val newTop = (b - t - pieSelectedRect.height()) / 2
        pieSelectedRect.offsetTo(newLeft, newTop)

        // размещение rect остальных зон
        val offsetDelta = pieSelectedRect.width() * zoomFactor / 2f
        pieRect.offsetTo(newLeft + offsetDelta, newTop + offsetDelta)
    }

    override fun onDraw(canvas: Canvas) {
        data?.also { list ->
            // начинать с 12 часов
            var startAngle = 270f
            var sweepAngle: Float
            var endAngle: Float

            // отрисовка секторов
            startAngles.clear()
            endAngles.clear()
            list.forEachIndexed { index, chartData ->
                // "длина" сектора
                sweepAngle = 360f * chartData.amount / totalSum
                // конец сектора
                endAngle = (startAngle + sweepAngle).let {
                    if (it < 360f) it else { it - 360f }
                }
                // цвет сектора
                piePaint.color = getColorByIndex(index)
                // отрисовать сектор с промежутками в 1 градус
                canvas.drawArc(
                    if (chartData.id == selectedId) pieSelectedRect else pieRect,
                    startAngle + 0.5f,
                    sweepAngle - 0.5f,
                    true,
                    piePaint
                )
                // сохранить данные для отслеживания тапов
                startAngles.add(startAngle)
                endAngles.add(endAngle)
                // к следующему
                startAngle = endAngle
            }
        }
    }

    /**
     * Устанавливает данные для чарта.
     *
     * @param value Список соответствующих объектов данных.
     */
    fun populate(value: List<ChartData>) {
        data = value
        totalSum = value.sumOf { it.amount }
        selectedId = value.getOrNull(0)?.id
        saySelected()
        invalidate()
    }


    private var onSelectCallback: ((id: Int?, color: Int?) -> Unit)? = null

    private fun saySelected() {
        onSelectCallback?.invoke(
            selectedId,
            selectedId?.let { getColorById(it) }
        )
    }

    /**
     * Вешает колбек на выбор сектора чарта.
     *
     * @param value Id - идентификатор выбранной категории, color - цвет сектора.
     */
    fun setOnSelect(value: (id: Int?, color: Int?) -> Unit) {
        onSelectCallback = value
        saySelected()
    }


    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(STATE_SUPER, super.onSaveInstanceState())
            // сохраняет генерацию цветов
            putIntArray(STATE_COLORS, colors)
            // сохраняет ID выбранного сектора
            putInt(STATE_SELECTED_ID, selectedId ?: -1)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            // использовать сохранённый ID, если он есть
            viewState.getInt(STATE_SELECTED_ID, -2).also { id ->
                // -2 - нет данных в стейте,
                // -1 - сектор не выбран
                selectedId = id.takeIf { it >= 0 }
            }
            // использовать сохранённые цвета, если они есть
            viewState.getIntArray(STATE_COLORS)?.also {
                colors = it
            }
            viewState = viewState.getParcelable(STATE_SUPER)
        }
        super.onRestoreInstanceState(viewState)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event)

}