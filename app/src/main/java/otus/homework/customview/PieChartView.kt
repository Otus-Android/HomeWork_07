package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var categoryExpenses: List<CategoryExpense> = emptyList()

    private var selectedCategory: String? = null

    private var onCategoryClickListener: ((String) -> Unit)? = null

    private val density = resources.displayMetrics.density

    private val defaultSize = 240f.dpToPx()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val chartBounds = RectF()

    private val colors = listOf(
        resources.getColor(R.color.brick, null),
        resources.getColor(R.color.yellow, null),
        resources.getColor(R.color.orange, null),
        resources.getColor(R.color.sea_wave, null),
        resources.getColor(R.color.blue, null),
        resources.getColor(R.color.sky, null),
        resources.getColor(R.color.pink, null),
        resources.getColor(R.color.violet, null),
        resources.getColor(R.color.red, null),
        resources.getColor(R.color.green, null),
        resources.getColor(R.color.lime, null),
        resources.getColor(R.color.pine, null)
    )

    init {
        isClickable = true
    }

    private fun Float.dpToPx(): Float {
        return this * density
    }

    fun setExpenses(expenses: List<Expense>) {

        val groupedExpenses = expenses
            .groupBy { it.category }
            .map { (category, items) ->
                category to items.sumOf { it.amount }
            }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }

        categoryExpenses = groupedExpenses.mapIndexed { index, (category, amount) ->

            CategoryExpense(
                category = category,
                totalAmount = amount,
                color = colors[index % colors.size]
            )
        }

        selectedCategory = selectedCategory?.takeIf { selected ->
            categoryExpenses.any { it.category == selected }
        }

        invalidate()
    }

    fun setOnCategoryClickListener(listener: (String) -> Unit) {
        onCategoryClickListener = listener
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {

        val desiredWidth = (defaultSize + paddingLeft + paddingRight).toInt()

        val desiredHeight = (defaultSize + paddingTop + paddingBottom).toInt()

        val measuredWidth = resolveMeasuredSize(
            desiredWidth,
            widthMeasureSpec
        )

        val measuredHeight = resolveMeasuredSize(
            desiredHeight,
            heightMeasureSpec
        )

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    private fun resolveMeasuredSize(
        desiredSize: Int,
        measureSpec: Int
    ): Int {

        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        return when (mode) {

            MeasureSpec.EXACTLY -> size

            MeasureSpec.AT_MOST -> min(desiredSize, size)

            MeasureSpec.UNSPECIFIED -> desiredSize

            else -> desiredSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (categoryExpenses.isEmpty()) return

        val totalAmount = categoryExpenses.sumOf {
            it.totalAmount
        }

        if (totalAmount <= 0.0) return

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val centerX = paddingLeft + contentWidth / 2f
        val centerY = paddingTop + contentHeight / 2f

        val radius = min(contentWidth, contentHeight) / 2f

        chartBounds.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = -90f

        categoryExpenses.forEach { category ->

            val sweepAngle = (category.totalAmount / totalAmount * 360.0).toFloat()

            paint.color = category.color
            paint.style = Paint.Style.FILL

            canvas.drawArc(
                chartBounds,
                startAngle,
                sweepAngle,
                true,
                paint
            )

            startAngle += sweepAngle
        }

        drawSelectedCategory(canvas)
    }

    private fun drawSelectedCategory(canvas: Canvas) {

        val selected = selectedCategory ?: return

        val totalAmount = categoryExpenses.sumOf {
            it.totalAmount
        }

        if (totalAmount <= 0.0) return

        var startAngle = -90f

        for (item in categoryExpenses) {

            val sweepAngle = (item.totalAmount / totalAmount * 360.0).toFloat()

            if (item.category == selected) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f.dpToPx()

                canvas.drawArc(
                    chartBounds,
                    startAngle,
                    sweepAngle,
                    true,
                    paint
                )

                paint.style = Paint.Style.FILL

                return
            }

            startAngle += sweepAngle
        }
    }

    private fun getCategoryAt(
        x: Float,
        y: Float
    ): CategoryExpense? {

        if (categoryExpenses.isEmpty()) return null

        val dx = x - chartBounds.centerX()
        val dy = y - chartBounds.centerY()

        val distance = sqrt(dx * dx + dy * dy)
        val radius = chartBounds.width() / 2f

        if (distance > radius) return null

        val angle = (
                Math.toDegrees(
                    atan2(dy.toDouble(), dx.toDouble())
                ) + 90.0 + 360.0
                ) % 360.0

        val totalAmount = categoryExpenses.sumOf {
            it.totalAmount
        }

        if (totalAmount <= 0.0) return null

        var currentAngle = 0.0

        for (category in categoryExpenses) {

            val sweepAngle = category.totalAmount / totalAmount * 360.0

            if (angle >= currentAngle && angle < currentAngle + sweepAngle) {
                return category
            }

            currentAngle += sweepAngle
        }

        return categoryExpenses.lastOrNull()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                return getCategoryAt(event.x, event.y) != null
            }

            MotionEvent.ACTION_UP -> {

                val category = getCategoryAt(event.x, event.y)

                if (category != null) {

                    selectedCategory = category.category

                    invalidate()

                    performClick()

                    onCategoryClickListener?.invoke(
                        category.category
                    )
                }

                return true
            }

            MotionEvent.ACTION_CANCEL -> return true
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private class SavedState : BaseSavedState {

        var selectedCategory: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedCategory = parcel.readString()
        }

        override fun writeToParcel(
            out: Parcel,
            flags: Int
        ) {
            super.writeToParcel(out, flags)
            out.writeString(selectedCategory)
        }

        companion object {

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(
                    source: Parcel
                ): SavedState = SavedState(source)

                override fun newArray(
                    size: Int
                ): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {

        return SavedState(super.onSaveInstanceState()).apply {
            selectedCategory = this@PieChartView.selectedCategory
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {

        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        selectedCategory = state.selectedCategory?.takeIf { selected ->
            categoryExpenses.any { it.category == selected }
        }

        invalidate()
    }
}