package otus.homework.customview.presentation.line.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import otus.homework.customview.presentation.line.chart.models.CursorStorage
import otus.homework.customview.presentation.line.chart.models.LineAreaProvider
import otus.homework.customview.presentation.line.chart.models.LineDataProvider
import otus.homework.customview.presentation.line.chart.models.LinePaints

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paints = LinePaints(resources)
    private val areaProvider = LineAreaProvider(paints)
    private val dataProvider = LineDataProvider(areaProvider)
    private val cursorStorage = CursorStorage(areaProvider)

    private lateinit var gradient: LinearGradient

    private val path = Path()

    private var isDebugMode = false

    private val labelRect = Rect()

    fun render(data: LineData) {
        dataProvider.calculate(data)
        invalidate()
    }

    fun setDebugMode(isEnabled: Boolean) {
        isDebugMode = isEnabled
        // TODO: можно ли дергать
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
        updateGradient()
        invalidate()
    }

    private fun updateGradient() {
        gradient = LinearGradient(
            areaProvider.local.left,
            areaProvider.local.top,
            areaProvider.local.left,
            areaProvider.local.bottom,
            Color.BLUE,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paints.gradient.shader = gradient
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun showToast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (cursorStorage.update(event.x, event.y)) {
                    invalidate()
                    val currentNode = dataProvider.getNodeByX(event.x)
                    showToast("${event.x} and ${event.y} ${currentNode?.date}")
                }
                true
            }

            MotionEvent.ACTION_MOVE -> {
                if (cursorStorage.update(event.x, event.y)) {
                    invalidate()
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                cursorStorage.clearCurrentLineX()
                invalidate()
                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isDebugMode) {
            drawDebugLayer(canvas)
            drawDebugGrid(canvas)
        }

        drawLines(canvas)
        drawVerticalCursor(canvas)
        drawLabel(canvas)
    }


    /** Нарисовать отладочную информацию по областям графика */
    private fun drawDebugLayer(canvas: Canvas) {
        canvas.drawRect(areaProvider.global, paints.global)
        canvas.drawRect(areaProvider.padding, paints.padding)
        canvas.drawRect(areaProvider.local, paints.local)
    }

    /** Нарисовать отладочную информацию по "сетке" */
    private fun drawDebugGrid(canvas: Canvas) {
        val numbers = 10
        val area = areaProvider.local
        val stepX = area.width() / numbers
        val stepY = area.height() / numbers
        var currentPointX = area.left
        var currentPointY = area.top
        for (i in 0 until numbers) {
            canvas.drawLine(currentPointX, area.bottom, currentPointX, area.top, paints.grid)
            canvas.drawText(currentPointX.toString(), currentPointX, area.bottom, paints.textAxis)

            canvas.drawLine(area.left, currentPointY, area.right, currentPointY, paints.grid)
            currentPointX += stepX
            currentPointY += stepY
        }
    }

    /** Нарисовать линии графика с градиентом */
    private fun drawLines(canvas: Canvas) {
        dataProvider.getNodes().takeIf { it.isNotEmpty() }?.let { nodes ->
            val area = areaProvider.local
            path.reset()

            // line
            path.moveTo(nodes.first().x, nodes.first().y)
            nodes.forEach { node -> path.lineTo(node.x, node.y) }

            canvas.drawPath(path, paints.line)

            // gradient
            path.lineTo(nodes.last().x, area.bottom)
            path.lineTo(area.left, area.bottom)
            path.close()

            canvas.drawPath(path, paints.gradient)
        }
    }

    /** Нарисовать вертикальную линию, соответствующую нажатой точке */
    private fun drawVerticalCursor(canvas: Canvas) {
        cursorStorage.getPoint()?.let { point ->
            canvas.drawLine(
                point.x,
                areaProvider.local.top,
                point.x,
                areaProvider.local.bottom,
                paints.currentLine
            )
        }
    }

    /** Нарисовать подпись, соответствующую нажатой точке */
    private fun drawLabel(canvas: Canvas) {
        val area = areaProvider.local
        dataProvider.getCurrentNode()?.label?.let { label ->
            canvas.drawText(label, area.centerX(), area.bottom, paints.text)
        }
    }
}