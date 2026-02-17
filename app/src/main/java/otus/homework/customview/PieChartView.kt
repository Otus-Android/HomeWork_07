package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

interface OnPieChartClickListener {
    fun onCategoryClicked(category: CategoryExpense)
}
private fun Context.loadPurchaseItemsFromJson(): List<PurchaseItem> {
    val resourceId = resources.getIdentifier("payload", "raw", packageName)
    val inputStream = resources.openRawResource(resourceId)
    val reader = InputStreamReader(inputStream)

    val categoryDeserializer = JsonDeserializer<CategoryExpense> { json, _, _ ->
        when (json.asString) {
            "Продукты" -> CategoryExpense.FOOD
            "Здоровье" -> CategoryExpense.HEALTH
            "Кафе и рестораны" -> CategoryExpense.RESTAURANT
            "Алкоголь" -> CategoryExpense.ALCOHOL
            "Доставка еды" -> CategoryExpense.DELIVERY
            "Транспорт" -> CategoryExpense.TRANSPORT
            "Спорт" -> CategoryExpense.SPORT
            else -> CategoryExpense.UNKNOWN
        }
    }

    val gson = GsonBuilder()
        .registerTypeAdapter(CategoryExpense::class.java, categoryDeserializer)
        .create()

    val listType = object : TypeToken<List<PurchaseItem>>() {}.type
    val items: List<PurchaseItem> = gson.fromJson(reader, listType)
    reader.close()
    return items
}

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val categoriesSum: MutableMap<CategoryExpense, Int> = mutableMapOf()
    private var totalAmount: Float = 0f
    private val colors = intArrayOf(
        Color.parseColor("#FF6384"),
        Color.parseColor("#36A2EB"),
        Color.parseColor("#FFCE56"),
        Color.parseColor("#4BC0C0"),
        Color.parseColor("#9966FF"),
        Color.parseColor("#FF9F40"),
        Color.parseColor("#E7E9ED"),
        Color.parseColor("#8D6E63"),
        Color.parseColor("#26A69A"),
        Color.parseColor("#EF5350"),
        Color.parseColor("#AB47BC"),
        Color.parseColor("#5C6BC0"),
        Color.parseColor("#26C6DA"),
        Color.parseColor("#D4E157"),
        Color.parseColor("#FFA270")
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
    }
    private var centerX = 0f
    private var centerY = 0f
    private var outerRadius = 0f
    private var innerRadius = 0f
    private var ringThickness = 0f
    private var radius = 0f
    private val bounds = Rect()
    var onPieChartClickListener: OnPieChartClickListener? = null

    init {
        setData(context.loadPurchaseItemsFromJson())
    }

    fun setData(items: List<PurchaseItem>) {
        categoriesSum.clear()
        for (item in items) {
            val category = item.category
            val amount = item.amount
            categoriesSum[category] = categoriesSum.getOrDefault(category, 0) + amount
        }
        totalAmount = categoriesSum.values.sum().toFloat()
        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSize = (resources.displayMetrics.density * 300).toInt()
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(MeasureSpec.getSize(widthMeasureSpec), minSize)
            else -> minSize
        }
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(MeasureSpec.getSize(heightMeasureSpec), minSize)
            else -> minSize
        }
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        outerRadius = minOf(centerX, centerY) * 0.8f
        ringThickness = outerRadius * 0.2f
        innerRadius = outerRadius - ringThickness
        radius = outerRadius - ringThickness / 2f
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (totalAmount == 0f || categoriesSum.isEmpty()) return

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ringThickness
        paint.strokeCap = Paint.Cap.BUTT

        var startAngle = 0f
        val entries = categoriesSum.entries.toList()
        for (i in entries.indices) {
            val (_, amount) = entries[i]
            val sweepAngle = 360f * amount / totalAmount
            paint.color = colors[i % colors.size]

            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle,
                sweepAngle,
                false,
                paint
            )

            val percent = (amount / totalAmount * 100).toInt()
            val text = "$percent%"
            textPaint.textSize = outerRadius * 0.15f
            val midAngle = startAngle + sweepAngle / 2f
            val textR = outerRadius + ringThickness * 0.8f
            val textX = centerX + textR * cos(Math.toRadians(midAngle.toDouble())).toFloat()
            val textY = centerY + textR * sin(Math.toRadians(midAngle.toDouble())).toFloat()

            textPaint.getTextBounds(text, 0, text.length, bounds)
            val offsetY = (bounds.top + bounds.bottom) / 2f
            val adjustedY = textY - offsetY

            canvas.drawText(text, textX, adjustedY, textPaint)

            startAngle += sweepAngle
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - centerX
            val y = event.y - centerY
            val distance = hypot(x.toDouble(), y.toDouble())
            if (distance !in innerRadius..outerRadius) return false

            var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
            if (angle < 0) angle += 360f

            var accumulatedAngle = 0f
            for ((category, amount) in categoriesSum) {
                val sweepAngle = 360f * amount / totalAmount
                if (angle >= accumulatedAngle && angle < accumulatedAngle + sweepAngle) {
                    onPieChartClickListener?.onCategoryClicked(category)
                    performClick()
                    return true
                }
                accumulatedAngle += sweepAngle
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            sums = HashMap(categoriesSum)
            total = totalAmount
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            categoriesSum.clear()
            state.sums?.let { categoriesSum.putAll(it) }
            totalAmount = state.total
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {
        var sums: HashMap<CategoryExpense, Int>? = null
        var total: Float = 0f

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            @Suppress("UNCHECKED_CAST")
            sums = parcel.readSerializable() as? HashMap<CategoryExpense, Int>
            total = parcel.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSerializable(sums)
            out.writeFloat(total)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}