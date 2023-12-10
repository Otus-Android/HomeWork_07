package otus.homework.customview.presentation.pie.chart

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.pie.chart.area.PieAreaStorage
import otus.homework.customview.presentation.pie.chart.cursor.CursorStorage
import otus.homework.customview.presentation.pie.chart.data.PieDataStorage
import otus.homework.customview.presentation.pie.chart.paints.PiePaints

/**
 * Круговой график
 */
class PieChartView constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    private val paints = PiePaints(resources)
    private val dataStorage = PieDataStorage()
    private val areaStorage = PieAreaStorage(paints)
    private val cursorStorage = CursorStorage(areaStorage, dataStorage)

    /** Стиль отображения кругового графика */
    var style: PieStyle
        get() = paints.style
        set(value) {
            paints.style = value
            areaStorage.updateChart()
            invalidate()
        }

    /** Признак отображения отладочной информации */
    var isDebugModeEnabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /** Нарисовать круговой график по данным [LineData] */
    fun render(pieData: PieData) {
        dataStorage.update(pieData)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        areaStorage.update(
            width = w,
            height = h,
            leftPadding = paddingLeft,
            topPadding = paddingTop,
            rightPadding = paddingRight,
            bottomPadding = paddingBottom
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (cursorStorage.update(event.x, event.y)) invalidate()
                true
            }

            MotionEvent.ACTION_UP -> {
                cursorStorage.clear()
                invalidate()
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                cursorStorage.clear()
                invalidate()
                false
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawSectors(canvas)
        drawCursor(canvas)
        drawLabel(canvas)

        if (isDebugModeEnabled) {
            drawDebugGrid(canvas)
            drawDebugAreas(canvas)
        }
    }

    /** Нарисовать сектора графика */
    private fun drawSectors(canvas: Canvas) {
        dataStorage.getNodes().takeIf { it.isNotEmpty() }?.forEach { node ->
            paints.pie.color = node.color
            if (node.startAngle != cursorStorage.getNode()?.startAngle) {
                canvas.drawArc(
                    areaStorage.default,
                    node.startAngle,
                    node.sweepAngle,
                    paints.style.isFilled,
                    paints.pie
                )
            }
        }
    }

    /** Выделить сектор, соответствующий позиции курсора */
    private fun drawCursor(canvas: Canvas) {
        cursorStorage.getNode()?.let { node ->
            paints.pie.color = node.color
            canvas.drawArc(
                areaStorage.expanded,
                node.startAngle + SELECTED_SECTOR_GAP_DEGREE,
                node.sweepAngle - 2 * SELECTED_SECTOR_GAP_DEGREE,
                paints.style.isFilled,
                paints.pie
            )
        }
    }


    /** Нарисовать подпись, соответствующую позиции курсора */
    private fun drawLabel(canvas: Canvas) {
        cursorStorage.getNode()?.label?.let { label ->
            canvas.drawText(
                label,
                areaStorage.default.centerX(),
                areaStorage.default.bottom,
                paints.label
            )
        }
    }

    /** Нарисовать отладочную информацию по областям графика */
    private fun drawDebugAreas(canvas: Canvas) {
        canvas.drawRect(areaStorage.global, paints.global)
        canvas.drawRect(areaStorage.padding, paints.padding)
        canvas.drawRect(areaStorage.chart, paints.chart)
        canvas.drawRect(areaStorage.default, paints.default)
    }

    /** Нарисовать отладочную информацию по "сетке" */
    private fun drawDebugGrid(canvas: Canvas) {
        val cellCount = DEBUG_CELL_COUNT
        val area = areaStorage.chart
        val stepX = area.width() / cellCount
        val stepY = area.height() / cellCount
        var currentPointX = area.left
        var currentPointY = area.top
        for (i in 0 until cellCount) {
            canvas.drawLine(currentPointX, area.bottom, currentPointX, area.top, paints.debugGrid)
            canvas.drawLine(area.left, currentPointY, area.right, currentPointY, paints.debugGrid)
            currentPointX += stepX
            currentPointY += stepY
        }
    }

    private companion object {

        /** Кол-во клеток "сетки" отладочной информации */
        const val DEBUG_CELL_COUNT = 10

        /** Градус отступа выбранного сектора */
        const val SELECTED_SECTOR_GAP_DEGREE = 1
    }
}