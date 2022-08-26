package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class StatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var countOfDays: Int = 31

    private var maxAmount: Int = 100_000

    private var countOfYAxes: Int = 10

    private val xInterval: Float
        get() = (measuredWidth - 2 * axeOffset) / (countOfDays - 1)

    private val yInterval: Float
        get() = (measuredHeight - 2 * axeOffset) / (countOfYAxes - 1)

    private val amountInterval: Float
        get() = maxAmount / (countOfYAxes - 1f)

    private val sizeOfInterval = 2f.dp
    private val sizeOff = 2f.dp
    private val axePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f.dp
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        pathEffect = DashPathEffect(floatArrayOf(sizeOfInterval, sizeOff), 0f)
    }
    private val axeOffset = 50f.dp

    private val daysPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 5f.dp
        textAlign = Paint.Align.CENTER
    }
    private val daysTopMargin = 8f.dp

    private val sumPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 5f.dp
        textAlign = Paint.Align.LEFT
    }
    private val amountLeftMargin = 4f.dp

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val measureSpecWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureSpecHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec)

        val currentWidth: Int = when (measureSpecWidthMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecWidth
            }
            else -> throw IllegalStateException("Incorrect Width")
        }

        val currentHeight: Int = when (measureSpecHeightMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecHeight
            }
            MeasureSpec.AT_MOST -> {
                measureSpecHeight
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecHeight
            }
            else -> throw IllegalStateException("Incorrect Height")
        }

        setMeasuredDimension(currentWidth, currentHeight)
    }

    override fun onDraw(canvas: Canvas) {
        drawGrid(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        drawXAxes(canvas)
        drawXAxeInfo(canvas)
        drawYAxes(canvas)
        drawYAxeInfo(canvas)
    }

    private fun drawXAxes(canvas: Canvas) {
        for (i in 0 until countOfDays) {
            canvas.drawLine(
                0f + axeOffset + (xInterval * i).toInt(),
                measuredHeight - axeOffset,
                0f + axeOffset + (xInterval * i).toInt(),
                0f + axeOffset,
                axePaint
            )
        }
    }

    private fun drawXAxeInfo(canvas: Canvas) {
        for (i in 0 until countOfDays) {
            canvas.drawText(
                "${i + 1}".padStart(2, '0'),
                0f + axeOffset + (xInterval * i).toInt(),
                measuredHeight - axeOffset + daysTopMargin,
                daysPaint
            )
        }

    }

    private fun drawYAxes(canvas: Canvas) {
        for (i in 0 until countOfYAxes) {
            canvas.drawLine(
                0f + axeOffset,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                measuredWidth - axeOffset,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                axePaint
            )
        }
    }

    private fun drawYAxeInfo(canvas: Canvas) {
        for (i in 0 until countOfYAxes) {
            canvas.drawText(
                (amountInterval * i).toInt().toString(),
                measuredWidth - axeOffset + amountLeftMargin,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                sumPaint
            )
        }
    }
}