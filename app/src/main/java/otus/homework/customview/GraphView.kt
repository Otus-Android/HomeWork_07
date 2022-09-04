package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val defaultSize = 100f
    private var rectF = RectF(0f, 0f, defaultSize, defaultSize)
    private val pointList: MutableList<Point> = mutableListOf(Point(0, 0))
    private var xScaleDivisionLength: Float = 0f
    private var yScaleDivisionLength: Float = 0f
    private val axisPaint = Paint()
    private val gridPaint = Paint()
    private var graphPath = Path()
    private val graphPaint = Paint()
    private val pointPaint = Paint()
    private val xAxisText = "Дни"
    private val yAxisText = "Сумма"
    private val textPaint = Paint()
    private val xAxisTextBounds = Rect()
    private val yAxisTextBounds = Rect()
    private var drawX = 0f
    private var drawY = 0f

    init {
        textPaint.apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 24F
        }
        axisPaint.apply {
            color = Color.BLACK
            isAntiAlias = true
            strokeWidth = 4f
        }
        gridPaint.apply {
            color = Color.LTGRAY
            isAntiAlias = true
            strokeWidth = 2f
        }
        graphPaint.apply {
            color = Color.RED
            flags = Paint.ANTI_ALIAS_FLAG
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        pointPaint.apply {
            color = Color.BLUE
            flags = Paint.ANTI_ALIAS_FLAG
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        textPaint.getTextBounds(xAxisText, 0, xAxisText.length, xAxisTextBounds)
        textPaint.getTextBounds(yAxisText, 0, yAxisText.length, yAxisTextBounds)
    }

    fun setData(pointList: List<Point>) {
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
                    if (xAxisDivision == 0) 20 else ((it.x - xMin) / xAxisDivision),
                    if (yAxisDivision == 0) 20 else ((it.y - yMin) / yAxisDivision)
                )
            }
            )
        }

        requestLayout()
        invalidate()
    }

    private fun calculateXScaleDivisionLength(): Float =
        (rectF.right - (rectF.left + yAxisTextBounds.height())) / 100f

    private fun calculateYScaleDivisionLength(): Float =
        (rectF.bottom - (rectF.top + xAxisTextBounds.height())) / 100f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getMode(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST,
            MeasureSpec.EXACTLY -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
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
        drawGrid(rectF, canvas)
        drawGraph(rectF, canvas)
    }

    private fun drawGraph(rectF: RectF, canvas: Canvas?) {
        canvas?.apply {
            with(rectF) {
                pointList.forEachIndexed { index, point ->
                    drawX =
                        (point.x * xScaleDivisionLength + left + yAxisTextBounds.height() + 5 * xScaleDivisionLength) * 0.9f
                    drawY =
                        (-point.y * yScaleDivisionLength + bottom - xAxisTextBounds.height() + yScaleDivisionLength) * 0.9f

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

    private fun drawGrid(rectF: RectF, canvas: Canvas?) {
        canvas?.apply {
            with(rectF) {
                (0..right.toInt() step 80).forEach {
                    drawLine(
                        left  + yAxisTextBounds.height() + it.toFloat(),
                        bottom - xAxisTextBounds.height(),
                        left  + yAxisTextBounds.height() + it.toFloat(),
                        top,
                        gridPaint
                    )
                }
                (0..(bottom - xAxisTextBounds.height()).toInt() step 80).forEach {
                    drawLine(
                        left + yAxisTextBounds.height(),
                        bottom - xAxisTextBounds.height() - it.toFloat(),
                        right,
                        bottom - xAxisTextBounds.height() - it.toFloat(),
                        gridPaint
                    )
                }
                drawLine(
                    left + yAxisTextBounds.height(),
                    bottom - xAxisTextBounds.height(),
                    right,
                    bottom - xAxisTextBounds.height(),
                    axisPaint
                )
                drawText(
                    xAxisText,
                    right - xAxisTextBounds.width() - 10,
                    bottom + xAxisTextBounds.height() + 2f - xAxisTextBounds.height() - 35,
                    textPaint
                )
                drawLine(
                    left + yAxisTextBounds.height(),
                    bottom - xAxisTextBounds.height(),
                    left + yAxisTextBounds.height(),
                    top,
                    axisPaint
                )
                save()
                rotate(-90f, left, top)
                drawText(
                    yAxisText,
                    left - yAxisTextBounds.width() - 10,
                    top - 4f + yAxisTextBounds.height() + 35,
                    textPaint
                )
                restore()
            }
        }
        if (xScaleDivisionLength == 0f || yScaleDivisionLength == 0f) {
            xScaleDivisionLength = calculateXScaleDivisionLength()
            yScaleDivisionLength = calculateYScaleDivisionLength()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }
}