package otus.homework.customview.view.linechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.Expense
import otus.homework.customview.R
import otus.homework.customview.view.PaintGenerator.getPaint

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var sizeX = MIN_SIZE
    private var sizeY = MIN_SIZE

    private var groupedItems: List<Expense> = emptyList()

    private var rectF: RectF = RectF()
    private val strokeSize = context.resources
        .getDimensionPixelSize(R.dimen.line_chart_stroke_width).toFloat()

    private val axisPaint = getPaint(0).apply {
        isDither = true
        color = Color.BLACK
        strokeWidth = strokeSize / 4
    }
    private val linePaint = getPaint(1).apply {
        isDither = true
        strokeWidth = strokeSize / 4
    }
    private val dotPaint = getPaint(2).apply {
        isDither = true
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = strokeSize
    }

    fun setItems(list: List<Expense>) {
        groupedItems = list
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = calculateSize(widthMeasureSpec)
        val heightSize = calculateSize(heightMeasureSpec)

        sizeX = resolveSize(widthSize, widthMeasureSpec)
        sizeY = resolveSize(heightSize, heightMeasureSpec)

        setMeasuredDimension(sizeX, sizeY)
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> MIN_SIZE
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> Integer.max(MIN_SIZE, size)
            else -> throw IllegalStateException("Invalid MeasureSpec mode")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startTop = 0f
        val startLeft = 0f
        val endBottom = height.toFloat()
        val rightBottom = height.toFloat()

        rectF.apply {
            top = startTop
            left = startLeft + strokeSize
            bottom = endBottom
            right = rightBottom - strokeSize
        }

        canvas.drawLine(0f, 0f, 0f, height.toFloat(), axisPaint)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint)

        if (groupedItems.isNotEmpty()) {
            val maxTime = groupedItems.maxOf { it.time }
            val maxAmount = groupedItems.maxOf { it.amount }

            val minTime = groupedItems.minOf { it.time }
            val minAmount = groupedItems.minOf { it.amount }

            var startX = 0f
            var startY = height.toFloat()
            groupedItems.sortedBy { it.time }.forEach {
                val pxX =
                    width.toFloat() * (it.time - minTime).toFloat() / (maxTime - minTime).toFloat()
                val pxY =
                    height.toFloat() * (it.amount - minAmount).toFloat() / (maxAmount - minAmount).toFloat()
                canvas.drawLine(startX, startY, pxX, pxY, linePaint)
                canvas.drawPoint(pxX, pxY, dotPaint)
                startX = pxX
                startY = pxY
            }
        }

    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle().apply {
            putParcelableArrayList(EXPENSES_TAG, ArrayList(groupedItems))
            putParcelable(BUNDLE_TAG, super.onSaveInstanceState())
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            groupedItems = state.getParcelableArrayList<Expense>(EXPENSES_TAG) as List<Expense>

            super.onRestoreInstanceState(state.getParcelable(BUNDLE_TAG))
        }
    }

    companion object {
        private const val MIN_SIZE = 300
        private const val STR = 300

        private const val EXPENSES_TAG = "expenses_tag"
        private const val BUNDLE_TAG = "bundle_tag"
    }
}