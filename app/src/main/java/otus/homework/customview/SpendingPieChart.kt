package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.*

class SpendingPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val TOTAL_AMOUNT_KEY = "total_amount_key"
        const val SPENDING_CATEGORY_LIST_KEY = "spending_category_map_key"
        const val SUPER_PARCELABLE_KEY = "super_parcelable"
    }

    private val spendingCategoryList = buildList {
        add(PieChartItem("category1", 1, Color.YELLOW, 0f, 36f, 200f))
        add(PieChartItem("category2", 2, Color.RED, 36f, 72f, 200f))
        add(PieChartItem("category3", 3, Color.GREEN, 108f, 108f, 200f))
        add(PieChartItem("category4", 4, Color.BLUE, 216f, 144f, 200f))
    }.toMutableList()

    private var onClickCategory: ((String) -> Unit)? = null

    private var totalAmount: Int = 10

    private val minSize = context.resources.getDimension(R.dimen.pie_chart_min_size).roundToInt()
    private var size = minSize
    private val priceChartRect = RectF()
    private val path = Path()
    private val pathMeasure = PathMeasure(path, true)

    private val sectorPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val sectorTextPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        color = Color.BLACK
        isAntiAlias = true
    }

    private val totalTextPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        color = Color.BLACK
        isAntiAlias = true
    }

    fun setExpenses(expenses: Expenses) {
        with(spendingCategoryList) {
            clear()
            addAll(expenses.toSpendingCategoryMap())
            totalAmount = sumOf(PieChartItem::amount)
        }

        requestLayout()
        invalidate()
    }

    fun setOnClickCategory(listener: (String) -> Unit) {
        onClickCategory = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = calculateSize(widthMeasureSpec)
        val heightSize = calculateSize(heightMeasureSpec)

        size = min(widthSize, heightSize)

        val pieStrokeWidth = size / 8f
        sectorPaint.strokeWidth = pieStrokeWidth
        setPriceChartRect(size, pieStrokeWidth)

        totalTextPaint.setTotalTextSize(totalAmount.toString(), size - 4 * pieStrokeWidth)

        with(sectorTextPaint) {
            textSize = pieStrokeWidth / 2
            spendingCategoryList.updateTextWidth(this)
        }

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        if (spendingCategoryList.isEmpty() || canvas == null) {
            return
        }

        spendingCategoryList.forEach { item ->

            canvas.drawArc(
                priceChartRect,
                item.startAngle,
                item.angle,
                false,
                sectorPaint.apply { color = item.color },
            )

            path.reset()
            path.addArc(priceChartRect, item.startAngle, item.angle)
            pathMeasure.setPath(path, false)

            if (pathMeasure.length > item.textWidth) {
                canvas.drawTextOnPath(
                    item.amount.toString(),
                    path,
                    0f,
                    sectorTextPaint.textSize / 2,
                    sectorTextPaint,
                )
            }
        }

        canvas.drawText(
            totalAmount.toString(),
            size.toFloat() / 2,
            size.toFloat() / 2 - ((totalTextPaint.descent() + totalTextPaint.ascent()) / 2),
            totalTextPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.onClick {
            getTouchPieChartAngle(event.x, event.y)?.let { angle ->
                for (item in spendingCategoryList) {
                    if (item.startAngle < angle && (item.startAngle + item.angle) >= angle) {
                        onClickCategory?.invoke(item.category)
                        break
                    }
                }
            }
        }

        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle =  bundleOf(
            TOTAL_AMOUNT_KEY to totalAmount,
            SPENDING_CATEGORY_LIST_KEY to spendingCategoryList,
        )

        bundle.putParcelable(SUPER_PARCELABLE_KEY, super.onSaveInstanceState())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            totalAmount = state.getInt(TOTAL_AMOUNT_KEY)
            val spendingList = state.getParcelableArrayList<PieChartItem>(SPENDING_CATEGORY_LIST_KEY)

            spendingList?.let { list ->
                spendingCategoryList.clear()
                spendingCategoryList.addAll(list)
            }

            super.onRestoreInstanceState(state.getParcelable(SUPER_PARCELABLE_KEY))
        }
    }

    private fun Expenses.toSpendingCategoryMap(): List<PieChartItem> {
        val totalAmount = sumOf(Spending::amount)
        var startAngle = 0f

        return groupBy(Spending::category)
            .map { entry ->
                val categoryAmount = entry.value.sumOf(Spending::amount)
                val angle = (360f / totalAmount) * categoryAmount

                val item = PieChartItem(
                    category = entry.key,
                    amount = categoryAmount,
                    color = getRandomColor(),
                    startAngle = startAngle,
                    angle = angle,
                    textWidth = 0f,
                )
                startAngle += item.angle
                item
            }
    }

    private fun getTouchPieChartAngle(touchedX: Float, touchedY: Float): Float? {
        val outerRadius = size / 2
        val innerRadius = outerRadius - sectorPaint.strokeWidth
        val centerX = size / 2
        val centerY = size / 2

        val distance = sqrt((centerX - touchedX).pow(2) + (centerY - touchedY).pow(2))

        if (distance < innerRadius || distance > outerRadius) {
            return null
        }

        val angle = Math.toDegrees(
            atan2(
                (centerY - touchedY).toDouble(),
                (centerX - touchedX).toDouble()
            )
        )

        return angle.toFloat() + 180
    }

    private fun MutableList<PieChartItem>.updateTextWidth(paint: Paint) {
        val newMap = map { pieChartItem ->
            val textWidth = paint.measureText(pieChartItem.amount.toString())
            pieChartItem.copy(textWidth = textWidth * 1.2f)
        }
        clear()
        addAll(newMap)
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> max(minSize, size)
            else -> throw IllegalStateException("Invalid MeasureSpec mode")
        }
    }

    private fun setPriceChartRect(size: Int, strokeWidth: Float) {
        val left = strokeWidth / 2
        val top = strokeWidth / 2
        val right = size - left
        val bottom = size - top
        priceChartRect.set(left, top, right, bottom)
    }

    private fun Paint.setTotalTextSize(text: String, desiredTextWidth: Float) {
        textSize = 100f

        val rect = Rect()
        getTextBounds(text, 0, text.length, rect)

        textSize = textSize * desiredTextWidth / rect.width()
    }

    private inline fun MotionEvent.onClick(callback: () -> Unit): MotionEvent {
        when (actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> callback()
        }
        return this
    }

    @Parcelize
    private data class PieChartItem(
        val category: String,
        val amount: Int,
        val color: Int,
        val startAngle: Float,
        val angle: Float,
        val textWidth: Float,
    ) : Parcelable
}