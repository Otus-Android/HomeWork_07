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
import otus.homework.customview.domain.Expense
import otus.homework.customview.presentation.line.chart.models.LineAreaProvider
import otus.homework.customview.presentation.line.chart.models.LineDataProvider
import otus.homework.customview.presentation.line.chart.models.LinePaints

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paints = LinePaints(resources)
    private val areaProvider = LineAreaProvider(paints)
    private val dataProvider = LineDataProvider(areaProvider)

    private lateinit var gradient: LinearGradient

    private var expenses : List<Expense> = emptyList()

    private val path = Path()

    private var isDebugMode = false

    private val labelRect = Rect()

    fun updateNodes(dataFrame: List<Expense>) {
        this.expenses = dataFrame
        dataProvider.calculatePrevious(dataFrame)
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

        // dataProvider.calculate(chart)
        dataProvider.calculatePrevious(expenses)
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
                if (dataProvider.updateCurrentLineX(event.x, event.y)) {
                    invalidate()
                    val currentNode = dataProvider.getCurrentNode()
                    showToast("${event.x} and ${event.y} ${currentNode?.date}")
                }
                true
            }

            MotionEvent.ACTION_MOVE -> {
                if (dataProvider.updateCurrentLineX(event.x, event.y)) {
                    invalidate()
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                dataProvider.clearCurrentLineX()
                invalidate()
                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isDebugMode) {
            drawDebugLayer(canvas)
            drawGrid(canvas)
        }
        drawLines(canvas)
        drawCurrentLineX(canvas)

        paints.textAxis.getTextBounds("12345", 0, "12345".length, labelRect)
        canvas.drawText(
            "12345",
            areaProvider.local.centerX(),
            areaProvider.local.bottom + labelRect.bottom,
            paints.textAxis
        )

        canvas.drawText(
            "12345",
            0f,
            labelRect.bottom.toFloat(),
            paints.textAxis
        )

        labelRect.offset(areaProvider.local.top.toInt(), areaProvider.local.left.toInt())
        canvas.drawRect(
            labelRect,
            paints.textAxis
        )
    }


    private fun drawDebugLayer(canvas: Canvas) {/* Stroke global */
        canvas.drawRect(areaProvider.global, paints.global)/* Stroke local */
        canvas.drawRect(areaProvider.padding, paints.padding)/* Stroke local */
        canvas.drawRect(areaProvider.local, paints.local)
    }

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

            val t = dataProvider.getCurrentNode()?.label ?: "empty"
            canvas.drawText(t, area.centerX(), area.bottom, paints.text)
        }
    }

    private fun drawCurrentLineX(canvas: Canvas) {
        dataProvider.getCurrentLineX()?.let { x ->
            canvas.drawLine(
                x, areaProvider.local.top, x, areaProvider.local.bottom, paints.currentLine
            )
        }
    }

    private fun drawGrid(canvas: Canvas) {
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
}