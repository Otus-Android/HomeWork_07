package otus.homework.customview.presentation.expensesgraph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px

class AxisDrawer(
    private val view: ExpensesGraphView
) {
    private val axisPaddingPx = 32.dp.px
    private val axisTickHeightPx = 10.dp.px
    private val gridDotSizePx = 2.dp.px
    private val tickCountX = 10
    private val tickCountY = 5

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 15f
        style = Paint.Style.FILL
    }

    private val axisTickPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    private val gridDotPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.FILL
    }

    internal fun drawAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - axisPaddingPx).toFloat()
        canvas.drawLine(
            axisPaddingPx.toFloat(), xAxisYPos, (view.width - axisPaddingPx).toFloat(), xAxisYPos, axisPaint
        )
        canvas.drawLine(
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            (view.height - axisPaddingPx).toFloat(),
            axisPaint
        )
    }

    internal fun drawTicksOnXAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - axisPaddingPx).toFloat()
        val xAxisXPos = (view.width - axisPaddingPx).toFloat()

        val tickStep = (xAxisXPos - axisPaddingPx) / tickCountX
        var currentX: Float = axisPaddingPx.toFloat() + tickStep

        repeat(tickCountX) {
            drawAxisXTick(canvas, currentX, xAxisYPos, axisTickHeightPx)
            currentX += tickStep
        }
    }

    private fun drawAxisXTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) =
        canvas.drawLine(tickX, tickStartY, tickX, tickStartY - tickHeight, axisTickPaint)

    internal fun drawTicksOnYAxis(canvas: Canvas) {
        val yAxisXPos = axisPaddingPx.toFloat()
        val yAxisYPos = (view.height - axisPaddingPx).toFloat()

        val tickStep = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStep

        repeat(tickCountY) {
            drawAxisYTick(canvas, yAxisXPos, currentY, axisTickHeightPx)
            currentY -= tickStep
        }
    }

    private fun drawAxisYTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) =
        canvas.drawLine(tickX, tickStartY, tickX + tickHeight, tickStartY, axisTickPaint)

    internal fun drawDotsAtTickIntersections(canvas: Canvas) {
        val gridDotsX = calculateGridDotsX()
        val gridDotsY = calculateGridDotsY()
        for (x in gridDotsX) {
            for (y in gridDotsY) {
                canvas.drawCircle(x, y, gridDotSizePx.toFloat(), gridDotPaint)
            }
        }
    }

    private fun calculateGridDotsX(): MutableList<Float> {
        val xTickPositions = mutableListOf<Float>()
        val xAxisXPos = (view.width - axisPaddingPx).toFloat()
        val tickStepX = (xAxisXPos - axisPaddingPx) / tickCountX
        var currentX: Float = axisPaddingPx.toFloat() + tickStepX
        repeat(tickCountX) {
            xTickPositions.add(currentX)
            currentX += tickStepX
        }
        return xTickPositions
    }

    private fun calculateGridDotsY(): MutableList<Float> {
        val yTickPositions = mutableListOf<Float>()
        val yAxisYPos = (view.height - axisPaddingPx).toFloat()
        val tickStepY = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStepY
        repeat(tickCountY) {
            yTickPositions.add(currentY)
            currentY -= tickStepY
        }
        return yTickPositions
    }
}