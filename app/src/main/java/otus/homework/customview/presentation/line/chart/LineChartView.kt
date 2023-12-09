package otus.homework.customview.presentation.line.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.presentation.line.chart.area.LineAreaProvider
import otus.homework.customview.presentation.line.chart.cursor.CursorStorage
import otus.homework.customview.presentation.line.chart.data.LineDataProvider
import otus.homework.customview.presentation.line.chart.paints.LinePaints

/**
 * Линейный график
 */
class LineChartView constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)


    private val areaProvider = LineAreaProvider()
    private val dataProvider = LineDataProvider(areaProvider)
    private val cursorStorage = CursorStorage(areaProvider)

    private val paints = LinePaints(areaProvider, resources)

    private val lineChartPath = Path()

    /** Признак отображения отладочной информации */
    var isDebugModeEnabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /** Нарисовать линйный график по данным [LineData] */
    fun render(data: LineData) {
        dataProvider.calculate(data)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        areaProvider.update(
            leftPosition = 0,
            topPosition = 0,
            rightPosition = w,
            bottomPosition = h,
            leftPadding = paddingLeft,
            topPadding = paddingTop,
            rightPadding = paddingRight,
            bottomPadding = paddingBottom
        )

        dataProvider.recalculate()
        paints.recalculate()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (cursorStorage.update(event.x, event.y)) invalidate()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                if (cursorStorage.update(event.x, event.y)) invalidate()
                true
            }

            MotionEvent.ACTION_UP -> {
                cursorStorage.clear()
                invalidate()
                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isDebugModeEnabled) {
            drawDebugAreas(canvas)
            drawDebugGrid(canvas)
        }

        drawLines(canvas)
        drawCursor(canvas)
        drawLabel(canvas)
    }


    /** Нарисовать отладочную информацию по областям графика */
    private fun drawDebugAreas(canvas: Canvas) {
        canvas.drawRect(areaProvider.global, paints.global)
        canvas.drawRect(areaProvider.padding, paints.padding)
        canvas.drawRect(areaProvider.local, paints.local)
    }

    /** Нарисовать отладочную информацию по "сетке" */
    private fun drawDebugGrid(canvas: Canvas) {
        val cellCount = DEBUG_CELL_COUNT
        val area = areaProvider.local
        val stepX = area.width() / cellCount
        val stepY = area.height() / cellCount
        var currentPointX = area.left
        var currentPointY = area.top
        for (i in 0 until cellCount) {
            canvas.drawLine(currentPointX, area.bottom, currentPointX, area.top, paints.debugGrid)
            canvas.drawText(
                currentPointX.toString(),
                currentPointX,
                area.bottom,
                paints.debugTextAxis
            )

            canvas.drawLine(area.left, currentPointY, area.right, currentPointY, paints.debugGrid)
            currentPointX += stepX
            currentPointY += stepY
        }
    }

    /** Нарисовать линии графика с градиентом */
    private fun drawLines(canvas: Canvas) {
        dataProvider.getNodes().takeIf { it.isNotEmpty() }?.let { nodes ->
            lineChartPath.reset()

            // линия
            val firstNode = nodes.first()
            lineChartPath.moveTo(firstNode.x, firstNode.y)
            nodes.forEach { node -> lineChartPath.lineTo(node.x, node.y) }
            canvas.drawPath(lineChartPath, paints.line)

            // градиент
            val area = areaProvider.local
            lineChartPath.lineTo(nodes.last().x, area.bottom)
            lineChartPath.lineTo(area.left, area.bottom)
            lineChartPath.close()
            canvas.drawPath(lineChartPath, paints.gradient)
        }
    }

    /** Нарисовать вертикальную линию, соответствующую нажатой точке */
    private fun drawCursor(canvas: Canvas) {
        cursorStorage.getPoint()?.let { point ->
            canvas.drawLine(
                point.x,
                areaProvider.local.top,
                point.x,
                areaProvider.local.bottom,
                paints.cursor
            )
        }
    }

    /** Нарисовать подпись, соответствующую позиции курсора */
    private fun drawLabel(canvas: Canvas) {
        val area = areaProvider.local
        val cursorPoint = cursorStorage.getPoint() ?: return
        val node = dataProvider.getNodeByX(cursorPoint.x)
        val label = node?.label ?: return
        canvas.drawText(label, area.centerX(), area.bottom, paints.label)
    }

    private companion object {

        /** Кол-во клеток "сетки" отладочной информации */
        const val DEBUG_CELL_COUNT = 10
    }
}