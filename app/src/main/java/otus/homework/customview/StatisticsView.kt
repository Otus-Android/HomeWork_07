package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt


class StatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var countOfDays: Int = 31

    private var maxSum: Int = 100_000

    private var countOfYChildAxes: Int = 10

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
    private val daysTextTopMargin = 8f.dp


    private val sumPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 5f.dp
        textAlign = Paint.Align.LEFT
    }
    private val sumTextLeftMargin = 4f.dp

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
        drawChildXAxes(canvas, countOfChildAxes = countOfDays)

        drawMainYAxe(canvas, countOfChildAxes = countOfYChildAxes)
        drawMainXAxe(canvas, countOfDays = countOfDays)
        drawChildYAxes(canvas, countOfChildAxes = countOfYChildAxes)
    }

    private fun drawMainXAxe(canvas: Canvas, countOfDays: Int) {
        canvas.drawLine(
            0f + axeOffset,
            measuredHeight - axeOffset,
            measuredWidth.toFloat() - axeOffset,
            measuredHeight - axeOffset,
            axePaint
        )

        val interval = (measuredWidth - 2 * axeOffset) / (countOfDays - 1)

        for (i in 0 until countOfDays) {
            val x = 0f + axeOffset + interval * i
            val y = measuredHeight - axeOffset
            canvas.drawText(
                (i+1).toString().padStart(2, '0'),
                x,
                y + daysTextTopMargin,
                daysPaint
            )
        }

    }

    private fun drawMainYAxe(canvas: Canvas, countOfChildAxes: Int) {
        canvas.drawLine(
            0f + axeOffset,
            measuredHeight - axeOffset,
            0f + axeOffset,
            0f + axeOffset,
            axePaint
        )

        val interval = (measuredHeight - 2 * axeOffset) / (countOfChildAxes - 1)

        val sumInterval = maxSum.toFloat() / (countOfChildAxes - 1)

        for (i in 0 until countOfChildAxes) {
            val x = measuredWidth - axeOffset + sumTextLeftMargin
            val y = measuredHeight - axeOffset - interval * i

            canvas.drawText(
                (sumInterval * i).toInt().toString(),
                x,
                y,
                sumPaint
            )
        }
    }

    private fun drawChildYAxes(canvas: Canvas, countOfChildAxes: Int) {

        canvas.drawLine(
            0f + axeOffset,
            0f + axeOffset,
            measuredWidth.toFloat() - axeOffset,
            0f + axeOffset,
            axePaint
        )

        val interval = (measuredHeight - 2 * axeOffset) / (countOfChildAxes - 1)

        for (i in 1 until  countOfChildAxes) {
            canvas.drawLine(
                0f + axeOffset,
                measuredHeight - axeOffset - (interval * i),
                measuredWidth.toFloat() - axeOffset,
                measuredHeight - axeOffset - (interval * i),
                axePaint
            )
        }
    }

    private fun drawChildXAxes(canvas: Canvas, countOfChildAxes: Int) {

        canvas.drawLine(
            measuredWidth - axeOffset,
            measuredHeight - axeOffset,
            measuredWidth - axeOffset,
            0f + axeOffset,
            axePaint
        )

        val interval = (measuredWidth - 2 * axeOffset) / (countOfChildAxes - 1)

        for (i in 1 until countOfChildAxes) {
            canvas.drawLine(
                0f + axeOffset + interval * i,
                measuredHeight - axeOffset,
                0f + axeOffset + interval * i,
                0f + axeOffset,
                axePaint
            )
        }
    }
}