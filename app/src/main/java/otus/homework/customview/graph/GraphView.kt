package otus.homework.customview.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import otus.homework.customview.R
import otus.homework.customview.dPToPx
import otus.homework.customview.paychart.PayChartSavedState

class GraphView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GraphView)

    private val defaultSize = 100f
    private var rectF = RectF(0f, 0f, defaultSize, defaultSize)
    private val pointList: MutableList<Point> = mutableListOf(Point(0f, 0f))
    private var xScaleDivisionLength: Float = 0f
    private var yScaleDivisionLength: Float = 0f
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f.dPToPx()
    }
    private val xAxisText = "Days"
    private val yAxisText = "Amount"
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = typedArray.getDimensionPixelSize(R.styleable.GraphView_text, 8.dPToPx()).toFloat()
    }
    private val xAxisTextBounds = Rect()
    private val yAxisTextBounds = Rect()

    private var drawX = 0f
    private var drawY = 0f
    private var graphPath = Path()
    private val graphPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f.dPToPx()
    }
    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f.dPToPx()
    }

    init {
        textPaint.getTextBounds(xAxisText, 0, xAxisText.length, xAxisTextBounds)
        textPaint.getTextBounds(yAxisText, 0, yAxisText.length, yAxisTextBounds)
    }

    fun setPoints(pointList: List<Point>) {
        val xList = pointList.sortedBy { it.x }
        val xMin = xList.first().x
        val xAxisDivision = (xList.last().x - xMin) / 100

        val yList = pointList.sortedBy { it.y }
        val yMin = yList.first().y
        val yAxisDivision = (yList.last().y - yMin) / 100

        this.pointList.apply {
            clear()
            addAll(pointList.sortedBy { it.x }.map {
                Point(
                    if (xAxisDivision == 0f) 20f else ((it.x - xMin) / xAxisDivision),
                    if (yAxisDivision == 0f) 20f else ((it.y - yMin) / yAxisDivision)
                )
            }
            )
        }

        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return GraphSavedState(pointList, state)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as GraphSavedState
        super.onRestoreInstanceState(ss.superState)
        pointList.clear()
        ss.list?.let { pointList.addAll(it) }
    }

    private fun calculateXScaleDivisionLength(): Float = (rectF.right - (rectF.left + yAxisTextBounds.height())) / 100f
    private fun calculateYScaleDivisionLength(): Float = (rectF.bottom - (rectF.top + xAxisTextBounds.height())) / 100f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val min = minOf(widthSize, heightSize)

        when {
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED -> setMeasuredDimension(min, min)
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.AT_MOST -> setMeasuredDimension(min, min)
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY -> setMeasuredDimension(heightSize, heightSize)
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.UNSPECIFIED -> setMeasuredDimension(min, min)
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST -> setMeasuredDimension(min, min)
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> setMeasuredDimension(widthSize, heightSize)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.UNSPECIFIED -> setMeasuredDimension(min, min)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> setMeasuredDimension(widthSize, heightSize)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val widthAddition = (width.toFloat() - measuredWidth) / 2
        val heightAddition = (height.toFloat() - measuredHeight) / 2
        rectF.let {
            it.left = widthAddition + paddingLeft
            it.top = heightAddition + paddingTop
            it.right = measuredWidth + widthAddition - paddingRight.toFloat()
            it.bottom = measuredHeight + heightAddition - paddingBottom.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawAxis(rectF, canvas)
        drawGraph(rectF, canvas)
    }

    private fun drawGraph(rectF: RectF, canvas: Canvas?) {
        canvas?.apply {
            with(rectF) {
                pointList.forEachIndexed { index, point ->
                    drawX = (point.x * xScaleDivisionLength + left + yAxisTextBounds.height() + 5 * xScaleDivisionLength) * 0.9f
                    drawY = (-point.y * yScaleDivisionLength + bottom - xAxisTextBounds.height() + yScaleDivisionLength) * 0.9f

                    drawPoint(drawX, drawY, pointPaint)
                    if (index == 0) {
                        graphPath = Path()
                        graphPath.moveTo(drawX, drawY)
                    } else {
                        graphPath.lineTo(drawX, drawY)
                    }
                }
                drawPath(graphPath, graphPaint)
            }
        }
    }

    private fun drawAxis(rectF: RectF, canvas: Canvas?) {
        canvas?.apply {
            with(rectF) {
                drawLine(
                    left + yAxisTextBounds.height(),
                    bottom - xAxisTextBounds.height(),
                    right,
                    bottom - xAxisTextBounds.height(),
                    paint
                )
                drawText(
                    xAxisText,
                    right - xAxisTextBounds.width(),
                    bottom + xAxisTextBounds.height() + 2f.dPToPx() - xAxisTextBounds.height(),
                    textPaint
                )
                drawLine(
                    left + yAxisTextBounds.height(),
                    bottom - xAxisTextBounds.height(),
                    left + yAxisTextBounds.height(),
                    top,
                    paint
                )
                save()
                rotate(-90f, left, top)
                drawText(yAxisText, left - yAxisTextBounds.width(), top - 4f.dPToPx() + yAxisTextBounds.height(), textPaint)
                restore()
            }
        }
        if (xScaleDivisionLength == 0f || yScaleDivisionLength == 0f) {
            xScaleDivisionLength = calculateXScaleDivisionLength()
            yScaleDivisionLength = calculateYScaleDivisionLength()
        }
    }
}