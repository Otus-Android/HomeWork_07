package otus.homework.customview.presentation.pie.chart

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.presentation.pie.chart.area.PieAreaProvider
import otus.homework.customview.presentation.pie.chart.cursor.CursorStorage
import otus.homework.customview.presentation.pie.chart.models.PieDataProvider
import otus.homework.customview.presentation.pie.chart.paints.PiePaints

class PieChartView constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    private val paints = PiePaints(resources)
    private val dataProvider = PieDataProvider()

    private val areaProvider = PieAreaProvider(paints)
    private val cursorStorage = CursorStorage(areaProvider, dataProvider)

    var style: PieStyle
        get() = paints.style
        set(value) {
            paints.style = value
            areaProvider.updateChart()
            invalidate()
        }

    /** Признак отображения отладочной информации */
    var isDebugModeEnabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    fun render(pieData: PieData) {
        dataProvider.calculate(pieData)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        areaProvider.update(
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
        dataProvider.getNodes().takeIf { it.isNotEmpty() }?.forEach { node ->
            paints.pie.color = node.color
            if (node.startAngle != cursorStorage.getNode()?.startAngle) {
                canvas.drawArc(
                    areaProvider.default,
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
                areaProvider.expanded,
                node.startAngle + SELECTED_SECTOR_GAP_DEGREE,
                node.sweepAngle - 2 * SELECTED_SECTOR_GAP_DEGREE,
                paints.style.isFilled,
                paints.pie
            )
        }
    }


    /** Нарисовать подпись, соответствующую позиции курсора */
    private fun drawLabel(canvas: Canvas) {
        cursorStorage.getNode()?.let { node ->
            canvas.drawText(
                node.label,
                areaProvider.default.centerX(),
                areaProvider.default.bottom,
                paints.label
            )
        }
    }

    /** Нарисовать отладочную информацию по областям графика */
    private fun drawDebugAreas(canvas: Canvas) {
        canvas.drawRect(areaProvider.global, paints.global)
        canvas.drawRect(areaProvider.padding, paints.padding)
        canvas.drawRect(areaProvider.chart, paints.chart)
        canvas.drawRect(areaProvider.default, paints.default)
    }

    /** Нарисовать отладочную информацию по "сетке" */
    private fun drawDebugGrid(canvas: Canvas) {
        val cellCount = DEBUG_CELL_COUNT
        val area = areaProvider.chart
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