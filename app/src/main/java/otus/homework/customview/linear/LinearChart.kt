package otus.homework.customview.linear

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import otus.homework.customview.R
import otus.homework.customview.models.DailySpendItem
import java.lang.Integer.max
import kotlin.math.ceil

class LinearChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val minLinearChartWidth =
        context.resources.getDimension(R.dimen.minLinearChartWidth).toInt()

    private val minLinearChartHeight =
        context.resources.getDimension(R.dimen.minLinearChartHeight).toInt()

    private val yAxisTextHeight = context.resources.getDimension(R.dimen.yAxisTextHeight).toInt()

    private val colorBlack = ContextCompat.getColor(context, R.color.black)
    private val colorBackground = ContextCompat.getColor(context, R.color.grayWithAlpha30)
    private val colorRed = ContextCompat.getColor(context, android.R.color.holo_red_dark)

    private val rowSize = context.resources.getDimension(R.dimen.rowSize).toInt()

    private val emptyChartText = context.resources.getString(R.string.emptyChartText)

    private var rowCount: Int = 0
    private var columnCount: Int = 0

    private val backgroundChartPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = colorBlack
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val backgroundChartTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = colorBackground
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    private val gridPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = ColorUtils.setAlphaComponent(Color.GRAY, 128)
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(2f, 4f), 50f)
        strokeWidth = 4f
    }

    private val axisPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = colorBlack
        textSize = 24f
    }

    private val chartPaint = Paint().apply {
        pathEffect = CornerPathEffect(60f)
        style = Paint.Style.STROKE
        color = colorRed
        strokeWidth = 6f
    }

    private val chartRect = Rect()
    private val yAxisTextRect = Rect()
    private val viewRect = Rect()

    private val chartPath = Path()
    private var xGridSize: Int = 0
    private var yGridSize: Int = 0

    private var items = listOf<DailySpendItem>()

    private val maxAmount: Int
        get() = items.maxBy { it.amount }.amount

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val viewWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val viewWidth = when (viewWidthMode) {
            MeasureSpec.UNSPECIFIED -> minLinearChartWidth

            MeasureSpec.AT_MOST -> max(minLinearChartWidth, viewWidthSize)

            MeasureSpec.EXACTLY -> max(minLinearChartWidth, viewWidthSize)

            else -> throw IllegalStateException("Unknown spec mode")
        }

        val viewHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val viewHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        val viewHeight = when (viewHeightMode) {
            MeasureSpec.UNSPECIFIED -> minLinearChartHeight

            MeasureSpec.AT_MOST -> max(minLinearChartHeight, viewHeightSize)

            MeasureSpec.EXACTLY -> max(minLinearChartHeight, viewHeightSize)

            else -> throw IllegalStateException("Unknown spec mode")
        }

        setMeasuredDimension(viewWidth, viewHeight)
    }

    fun setData(items: List<DailySpendItem>) {
        this.items = items

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        calculateViewRect(canvas)

        drawBackground(canvas)
        drawGrid(canvas)
        drawChart(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val savedState = SavedState(superState)

        savedState.items = items

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        items = state.items

        invalidate()
    }

    private fun calculateViewRect(canvas: Canvas) {
        canvas.getClipBounds(viewRect)

        chartRect.set(0, 0, measuredWidth, measuredHeight - yAxisTextHeight)

        yAxisTextRect.set(0, measuredHeight - yAxisTextHeight, measuredWidth, measuredHeight)
    }

    private fun drawChart(canvas: Canvas) {
        if (items.isEmpty()) return

        chartPath.reset()

        items.forEachIndexed { index, item ->
            chartPath.apply {

                val yGridValue = item.amount.evaluateOrZero {
                    it / maxAmount.toFloat() * chartRect.height()
                }

                if (index == 0) {
                    moveTo(chartRect.left.toFloat(), chartRect.bottom - yGridValue)
                } else {
                    val dxChartPoint = chartRect.left.toFloat() + index * xGridSize
                    val dyChartPoint = chartRect.bottom.toFloat() - yGridValue

                    lineTo(dxChartPoint, dyChartPoint)
                }
            }
        }

        canvas.drawPath(chartPath, chartPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(viewRect, backgroundChartPaint)

        val x = viewRect.centerX().toFloat()
        val y = viewRect.centerY() -
                ((backgroundChartTextPaint.descent() + backgroundChartTextPaint.ascent()) / 2)

        val text = if (items.isEmpty()) {
            emptyChartText
        } else {
            items.first().category
        }

        canvas.drawText(text, x, y, backgroundChartTextPaint)
    }

    private fun drawGrid(canvas: Canvas) {
        if (items.isEmpty()) return

        columnCount = items.size
        rowCount = ceil(measuredHeight.toDouble() / rowSize).toInt()

        xGridSize = chartRect.width() / columnCount
        yGridSize = chartRect.height() / rowCount

        var x = 0f
        var y = chartRect.bottom.toFloat()

        //draw X-Axis
        val dyTextXAxis = yAxisTextRect.centerY() - ((axisPaint.descent() + axisPaint.ascent()) / 2)

        val daysDelta = ceil(items.size / columnCount.toFloat()).toInt()

        repeat(columnCount) { index ->
            val dxGrid = chartRect.left.toFloat() + index * xGridSize
            canvas.drawLine(dxGrid, 0f, dxGrid, y, gridPaint)

            val textToDraw = (index * daysDelta + 1).toString()
            val textToDrawWidth = axisPaint.measureText(textToDraw)

            val dxTextXAxis = if (index == 0) {
                -5f
            } else {
                textToDrawWidth * 0.5f
            }

            canvas.drawText(textToDraw, x - dxTextXAxis, dyTextXAxis, axisPaint)

            x += xGridSize
        }

        //draw Y-Axis
        val ySpendStep = maxAmount / rowCount

        repeat(rowCount + 1) { index ->
            canvas.drawLine(0f, y, chartRect.width().toFloat(), y, gridPaint)

            val textToDraw = "${index * ySpendStep} $"
            val textToDrawWidth = axisPaint.measureText(textToDraw)

            val dxTextYAxis = chartRect.width() - textToDrawWidth - 5f

            val dyTextYAxis = if (index == rowCount) {
                axisPaint.textSize + 5f
            } else {
                y - 5f
            }

            canvas.drawText(textToDraw, dxTextYAxis, dyTextYAxis, axisPaint)

            y -= yGridSize
        }
    }

    private fun Int.evaluateOrZero(block: (value: Int) -> Float): Float = try {
        block(this)
    } catch (t: Throwable) {
        if (t is ArithmeticException) {
            0f
        } else {
            throw t
        }
    }

    internal class SavedState : BaseSavedState {
        var items = listOf<DailySpendItem>()

        constructor(superState: Parcelable?) : super(superState)
        private constructor(parcel: Parcel) : super(parcel) {
            parcel.readList(items, List::class.java.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)

            out.writeList(items)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : Parcelable.Creator<SavedState?> {
                    override fun createFromParcel(parcel: Parcel): SavedState {
                        return SavedState(parcel)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}
