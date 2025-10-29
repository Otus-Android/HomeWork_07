package otus.homework.customview.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave
import otus.homework.customview.data.CategoryColor
import otus.homework.customview.data.CategorySlice
import otus.homework.customview.data.Transaction
import kotlin.math.atan2
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.WHITE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 80f
    }

    private var rectF = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private var transactions = listOf<Transaction>()
    private var slices = mutableListOf<CategorySlice>()
    private var selectedSlice: CategorySlice? = null

    private var onCategoryClickListener: ((CategorySlice) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (CategorySlice) -> Unit) {
        onCategoryClickListener = listener
    }

    fun setTransactions(items: List<Transaction>) {
        transactions = items
        processData()
        invalidate()
    }

    private fun processData() {
        slices.clear()

        if (transactions.isEmpty()) {
            return
        }

        // Group transactions by category and calculate totals
        val groupedTransactions = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { transaction -> transaction.amount } }

        val totalAmount = groupedTransactions.values.sum().toFloat()

        // Start pie from top
        var currentAngle = -90f

        // Create category pie slices with color based on category index
        slices = groupedTransactions.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val percentage = entry.value / totalAmount
                val sweepAngle = percentage * 360f
                currentAngle += sweepAngle
                CategorySlice(
                    name = entry.key,
                    amount = entry.value,
                    percentage = percentage * 100, // Human-readable
                    color = CategoryColor.entries[index % CategoryColor.entries.size].colorCode,
                    startAngle = currentAngle - sweepAngle,
                    sweepAngle = sweepAngle,
                )
            }.toMutableList()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Explicit width measurement example
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val desiredWidth = (resources.displayMetrics.density * 360).toInt()

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth // MeasureSpec.UNSPECIFIED
        }

        // Simplified height measurement example
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        // Combined result
        setMeasuredDimension(width, resolveSize(minHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 24f
        centerX = w / 2f
        centerY = h / 2f
        radius = (minOf(w, h) / 2f) - padding

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (slices.isEmpty()) {
            canvas.drawText("No data available", centerX, centerY, textPaint)
            return
        }

        slices.forEach { slice ->
            if (slice == selectedSlice) slicePaint.color = Color.BLACK
            else slicePaint.color = slice.color
            canvas.withSave {
                drawArc(
                    rectF,
                    slice.startAngle,
                    slice.sweepAngle,
                    true,
                    slicePaint
                )
                drawArc(
                    rectF,
                    slice.startAngle,
                    slice.sweepAngle,
                    true,
                    separatorPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val slice = getSliceAtPoint(event.x, event.y)
            if (slice != null) {
                selectedSlice = slice
                selectedSlice?.let {
                    onCategoryClickListener?.invoke(it)
                    invalidate()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun getSliceAtPoint(x: Float, y: Float): CategorySlice? {
        val dx = x - centerX
        val dy = y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > radius) return null

        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (angle < 0) angle += 360f
        angle = (angle + 90) % 360 // Adjust for starting angle

        var currentAngle = 0f
        slices.forEach { slice ->
            if (angle >= currentAngle && angle < (currentAngle + slice.sweepAngle)) {
                return slice
            }
            currentAngle += slice.sweepAngle
        }
        return null
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putParcelable("selectedSlice", selectedSlice)
            putParcelableArrayList("transactions", ArrayList(transactions))
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            selectedSlice = state.getParcelable("selectedSlice")
            val items = state.getParcelableArrayList<Transaction>("transactions")
            items?.let { setTransactions(it) }
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
