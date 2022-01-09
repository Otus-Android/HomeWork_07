package otus.homework.customview.linegraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.data.PurchasesData

class LineGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val lineGraphData = LineGraphData()

    private val padding = 20f
    private val defaultSize = 500
    private var currentSize = defaultSize
    private val lineStep = 100

    private var category: String? = null
    private val firstDate = 1622494800
    private val lastDate = 1625086800
    private val oneDay = 86400

    private val axisPaint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val axisLinePaint: Paint = Paint().apply {
        color = Color.LTGRAY
    }
    private val linePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 10f
        isAntiAlias = true
    }

    private val textPaint: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        isAntiAlias = true
    }
    private val textPaintDesc: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT

    }

    private val purchasesData = PurchasesData()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var size =
            minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        if (size < defaultSize) size = defaultSize

        currentSize = if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, defaultSize)
            defaultSize
        } else {
            setMeasuredDimension(size, size)
            size
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        category?.let {
            drawAxes(canvas, it)

            var lastX = padding
            var lastY = currentSize - padding
            var stopYdef = lastY
            if (lineGraphData.path.isEmpty()) {
                val pairOfPurchase = purchasesData.getPurchasesForLineGraph(context, it)
                val maxAmountInCategory = pairOfPurchase.first + 200

                for (date in firstDate until lastDate step oneDay) {

                    var drawLine = false

                    for (order in pairOfPurchase.second) {

                        if (order.time.toLong() > date && order.time.toLong() <= date + oneDay) {
                            val stopY =
                                (currentSize - padding) * (1 - (order.amount.toFloat() / maxAmountInCategory.toFloat()))
                            drawLine = true
                            canvas.drawLine(lastX, lastY, lastX + 30, stopY, linePaint)
                            lastX += 30
                            lastY = stopY
                            lineGraphData.addToPath(lastX, lastY)
                            canvas.drawText(
                                order.amount,
                                lastX + padding,
                                lastY,
                                textPaintDesc
                            )
                        } else {
                            drawLine = false
                        }

                    }
                    if (!drawLine) {
                        canvas.drawLine(lastX, lastY, lastX + 30, stopYdef, linePaint)
                        lastX += 30
                        lastY = stopYdef
                        lineGraphData.addToPath(lastX, lastY)
                    }

                    drawTexts(canvas)
                }
            }
        }
    }

    private fun drawAxes(canvas: Canvas, category: String) {

        canvas.drawLine(
            padding,
            currentSize - padding,
            currentSize - padding,
            currentSize - padding,
            axisPaint
        )
        canvas.drawLine(padding, currentSize - padding, padding, padding, axisPaint)
        if (lineGraphData.linesY.isEmpty()) {
            val pairOfPurchase = purchasesData.getPurchasesForLineGraph(context, category)
            val maxAmountInCategory = pairOfPurchase.first
            if (maxAmountInCategory > lineStep) {
                for (y in lineStep until maxAmountInCategory step lineStep) {
                    val lineY =
                        (currentSize - padding) * (1 - (y.toFloat() / maxAmountInCategory.toFloat()))
                    canvas.drawLine(padding, lineY, currentSize - padding, lineY, axisLinePaint)
                    lineGraphData.addToLinesY(lineY)
                }
            }
        } else {
            for (lineY in lineGraphData.linesY) {
                canvas.drawLine(padding, lineY, currentSize - padding, lineY, linePaint)
            }
        }
    }

    fun setCategory(value: String) {
        category = value
        lineGraphData.clear()
        invalidate()
    }

    private fun drawTexts(canvas: Canvas) {
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Расходы", padding * 2, padding * 2, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Дата", currentSize - padding * 2, currentSize - padding * 2, textPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putSerializable("lineGraphData", lineGraphData)
        bundle.putParcelable("suplineGraphData", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            val restoreState = viewState.getSerializable("lineGraphData") as LineGraphData
            viewState = viewState.getParcelable("suplineGraphData")

        }
        super.onRestoreInstanceState(viewState)
    }

}