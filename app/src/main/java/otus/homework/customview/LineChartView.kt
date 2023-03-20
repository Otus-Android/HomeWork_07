package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.view.doOnLayout

private const val MIN_SIZE_DP = 300
private const val SIDES_RATIO = 1.7f
private const val GRID_LINE_STEP_DP = 23
private const val CHART_PADDING_DP = 32
private const val CHART_POINT_RADIUS_DP = 4
private const val SCALE_MARKS_CORNER_RADIUS_DP = 4
class LineChartView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val Int.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    private val gridLinesPaint: Paint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1.dp
        pathEffect = DashPathEffect(floatArrayOf(2.dp, 1.dp), 0f)
    }
    private val scaleLinesPaint: Paint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2.dp
        strokeCap = Paint.Cap.ROUND
    }
    private val chartLinePaint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 2.dp
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(4.dp)
        strokeCap = Paint.Cap.ROUND
    }
    private val chartPointPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val categoryNamePaint: Paint = Paint().apply {
        textSize = 16.dp
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val scaleMarksPaint: Paint = Paint().apply {
        textSize = 10.dp
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val scaleMarksBackgroundFillPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val scaleMarksBackgroundStrokePaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
    }
    private var categoryName: String = ""
    private val linePath: Path = Path()
    private val pointPath: Path = Path()
    private val chartAreaRect = RectF()
    private val categoryNameRect = Rect()
    private val scaleMarks: MutableList<ScaleMarkModel> = mutableListOf()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // check all available width/height combinations
        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    MIN_SIZE_DP.dp.toInt(),
                    (MIN_SIZE_DP.dp / SIDES_RATIO).toInt()
                )
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(MIN_SIZE_DP.dp.toInt()),
                    heightSize.coerceAtLeast((widthSize / SIDES_RATIO).toInt())
                )
                /*chartSide = widthSize
                    .toFloat()
                    .coerceAtLeast(MIN_SIZE.dp)
                    .coerceAtMost(heightSize.toFloat())*/
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(MIN_SIZE_DP.dp.toInt()),
                    heightSize.coerceAtLeast((widthSize / SIDES_RATIO).toInt())
                )
                /*chartSide = widthSize
                    .toFloat()
                    .coerceAtLeast(MIN_SIZE.dp)
                    .coerceAtMost(heightSize.toFloat())*/
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(MIN_SIZE_DP.dp.toInt()),
                    heightSize.coerceAtLeast((widthSize / SIDES_RATIO).toInt())
                )
                /*chartSide = widthSize
                    .toFloat()
                    .coerceAtLeast(MIN_SIZE.dp)
                    .coerceAtMost(heightSize.toFloat())*/
            }
            else -> {
                // nothing to do
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chartAreaRect.left = CHART_PADDING_DP.dp
        chartAreaRect.top = CHART_PADDING_DP.dp
        chartAreaRect.right = width - CHART_PADDING_DP.dp
        chartAreaRect.bottom = height - CHART_PADDING_DP.dp
    }

    override fun onDraw(canvas: Canvas) {
        //canvas.drawColor(Color.GRAY)

        var verticalOffset = chartAreaRect.bottom
        var horizontalOffset = chartAreaRect.left
        while (verticalOffset > chartAreaRect.top) {
            canvas.drawLine(chartAreaRect.left, verticalOffset, chartAreaRect.right, verticalOffset, gridLinesPaint)
            verticalOffset -= GRID_LINE_STEP_DP.dp
        }
        while (horizontalOffset < chartAreaRect.right) {
            canvas.drawLine(horizontalOffset, chartAreaRect.top, horizontalOffset, chartAreaRect.bottom, gridLinesPaint)
            horizontalOffset += GRID_LINE_STEP_DP.dp
        }

        canvas.drawLine(
            chartAreaRect.left,
            chartAreaRect.bottom,
            chartAreaRect.right,
            chartAreaRect.bottom,
            scaleLinesPaint
        )
        canvas.drawLine(
            chartAreaRect.left,
            chartAreaRect.bottom,
            chartAreaRect.left,
            chartAreaRect.top,
            scaleLinesPaint
        )
        canvas.drawPath(linePath, chartLinePaint)
        canvas.drawPath(pointPath, chartPointPaint)
        canvas.drawText(
            categoryName,
            categoryNameRect.width() / 2f + CHART_PADDING_DP.dp,
            (CHART_PADDING_DP.dp + categoryNameRect.height()) / 2,
            categoryNamePaint
        )

        scaleMarks.forEachIndexed { index, value ->
            if (index.isOdd()) {
                scaleMarksPaint.getTextBounds(
                    value.markText,
                    0, value.markText.length,
                    value.rect
                )
                val textWidth = value.rect.width() + 4.dp
                val textHeight = value.rect.height() + 4.dp
                val textBackgroundLeft = value.markTextXPos
                val textBackgroundTop = value.markTextYPos - textHeight
                val textBackgroundRight = value.markTextXPos + textWidth
                val textBackgroundBottom = value.markTextYPos
                canvas.drawRoundRect(
                    textBackgroundLeft,
                    textBackgroundTop,
                    textBackgroundRight,
                    textBackgroundBottom,
                    SCALE_MARKS_CORNER_RADIUS_DP.dp,
                    SCALE_MARKS_CORNER_RADIUS_DP.dp,
                    scaleMarksBackgroundFillPaint
                )
                canvas.drawRoundRect(
                    textBackgroundLeft,
                    textBackgroundTop,
                    textBackgroundRight,
                    textBackgroundBottom,
                    SCALE_MARKS_CORNER_RADIUS_DP.dp,
                    SCALE_MARKS_CORNER_RADIUS_DP.dp,
                    scaleMarksBackgroundStrokePaint
                )
                canvas.drawText(
                    value.markText,
                    value.markTextXPos + textWidth / 2,
                    value.markTextYPos - 3.dp,
                    scaleMarksPaint
                )
            } else {
                canvas.drawText(
                    value.markText,
                    value.markTextXPos,
                    value.markTextYPos,
                    scaleMarksPaint
                )
            }

        }
    }

    fun setData(
        categoryName: String,
        spendingByTimeData: Map<String, Int>
    ) {
        this.categoryName = categoryName

        doOnLayout {
            categoryNamePaint.getTextBounds(categoryName, 0, categoryName.length, categoryNameRect)
            val maxAmount = spendingByTimeData.maxBy { it.value }.value
            val stepVertical = chartAreaRect.height() / maxAmount
            val stepHorizontal = chartAreaRect.width() / spendingByTimeData.size
            var initHorizontalPos = chartAreaRect.left

            linePath.apply {
                spendingByTimeData.forEach {
                    val x = initHorizontalPos
                    val y = chartAreaRect.bottom - stepVertical * it.value
                    if (x == chartAreaRect.left) moveTo(x, y) else lineTo(x, y)
                    initHorizontalPos += stepHorizontal
                    scaleMarks.add(
                        ScaleMarkModel(
                            markText = it.key,
                            markTextXPos = x,
                            markTextYPos = chartAreaRect.bottom + CHART_PADDING_DP.dp / 2
                        )
                    )
                    scaleMarks.add(
                        ScaleMarkModel(
                            markText = "${it.value} $",
                            markTextXPos = x,
                            markTextYPos = y - 10.dp
                        )
                    )
                    pointPath.apply {
                        addOval(
                            x - CHART_POINT_RADIUS_DP.dp,
                            y - CHART_POINT_RADIUS_DP.dp,
                            x + CHART_POINT_RADIUS_DP.dp,
                            y + CHART_POINT_RADIUS_DP.dp,
                            Path.Direction.CW
                        )
                    }
                }
            }
            invalidate()
        }
    }
    private fun Int.isOdd(): Boolean {
        return this % 2 != 0
    }
    private inner class ScaleMarkModel(
        val markText: String,
        val markTextXPos: Float,
        val markTextYPos: Float,
        val rect: Rect = Rect()
    )

}