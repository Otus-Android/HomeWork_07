package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var segments = listOf<SegmentData>()
    private val colorResIds = intArrayOf(
        R.color.lemon,
        R.color.orange_900,
        R.color.raspberry,
        R.color.purple_200,
        R.color.purple_500,
        R.color.purple_700,
        R.color.teal_200,
        R.color.teal_700,
        R.color.black,
        R.color.white,
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = SEGMENT_HEIGHT
        isAntiAlias = true
    }
    private val rectF = RectF()
    var onSegmentClickListener: ((category: String) -> Unit)? = null
    private var selectedCategory: String? = null
    private var selectedAmount: String? = null
    private var selectedPercent: String? = null
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            this.sums = segments.map { it.amount }.toIntArray()
            this.colors = segments.map { it.color }.toIntArray()
            this.categories = segments.map { it.category }.toTypedArray()
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val sums = state.sums ?: intArrayOf()
            val colors = state.colors ?: intArrayOf()
            val cats = state.categories ?: arrayOf()
            segments = sums.indices.map { i ->
                SegmentData(sums[i], colors[i], cats.getOrElse(i) { "" })
            }
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setData(data: List<Transaction>) {
        getCategoriesFromTransactions(data)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val optimalSize = (minOf(screenWidth, screenHeight) * CHART_PERCENT_OF_FULL_SIZE).toInt()
        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> minOf(optimalSize, wSize)
            MeasureSpec.UNSPECIFIED -> optimalSize
            else -> wSize
        }
        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> minOf(optimalSize, hSize)
            MeasureSpec.UNSPECIFIED -> optimalSize
            else -> hSize
        }
        val finalSize = minOf(width, height)
        setMeasuredDimension(finalSize, finalSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return
        val size = minOf(width, height).toFloat()
        val offset = paint.strokeWidth / 2
        rectF.set(offset, offset, size - offset, size - offset)
        val total = segments.sumOf { it.amount }
        if (total == 0) return
        var startAngle = -90f
        for (segment in segments) {
            val currentAngle = (segment.amount.toFloat() / total) * 360f
            paint.color = segment.color
            canvas.drawArc(rectF, startAngle, currentAngle, false, paint)
            startAngle += currentAngle
        }
        selectedCategory?.let { category ->
            val centerX = width / 2f
            val centerY = height / 2f
            val lineSpacing = CLICKED_INFO_LIMES_PACING
            canvas.drawText(category, centerX, centerY - lineSpacing, textPaint)
            var amountPercentText = selectedAmount.orEmpty()
            if (!selectedPercent.isNullOrEmpty()) {
                amountPercentText += " - $selectedPercent%"
            }
            canvas.drawText(amountPercentText, centerX, centerY + textPaint.textSize, textPaint)
        }
    }

    fun getCategoriesFromTransactions(transactions: List<Transaction>) {
        val grouped = transactions.groupBy { it.category }
        segments = grouped.entries.mapIndexed { index, entry ->
            val sum = entry.value.sumOf { it.amount }
            val resId = colorResIds.getOrElse(index % colorResIds.size) { Color.WHITE }
            val color = ContextCompat.getColor(context, resId)
            SegmentData(
                amount = sum,
                color = color,
                category = entry.key
            )
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerX = width / 2f
            val centerY = height / 2f
            val dx = event.x - centerX
            val dy = event.y - centerY
            val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val outerRadius = rectF.width() / 2 + paint.strokeWidth / 2
            val innerRadius = rectF.width() / 2 - paint.strokeWidth / 2
            if (distance !in innerRadius..outerRadius) {
                return false
            }
            val rawAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
            val angle = (rawAngle + 360) % 360
            val segmentAngle = (angle + 90 + 360) % 360
            val total = segments.sumOf { it.amount.toLong() }.toFloat()
            if (total > 0f) {
                var currentAngle = 0f
                for (segment in segments) {
                    val currentPercent = segment.amount.toFloat() / total
                    val segmentAngel = currentPercent * 360f
                    if (segmentAngle >= currentAngle && segmentAngle < (currentAngle + segmentAngel)) {
                        selectedCategory = segment.category
                        selectedAmount = segment.amount.toString()
                        selectedPercent = (currentPercent * 100).roundToInt().toString()
                        invalidate()
                        onSegmentClickListener?.invoke(segment.category)
                        performClick()
                        return true
                    }
                    currentAngle += segmentAngel
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
    private companion object {
        const val SEGMENT_HEIGHT = 140f
        const val CHART_PERCENT_OF_FULL_SIZE = 0.6f
        const val CLICKED_INFO_LIMES_PACING = 10f
    }

    private class SavedState : BaseSavedState {
        var sums: IntArray? = null
        var colors: IntArray? = null
        var categories: Array<String>? = null

        constructor(superState: Parcelable?) : super(superState)
        constructor(source: Parcel) : super(source) {
            sums = source.createIntArray()
            colors = source.createIntArray()
            categories = source.createStringArray()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeIntArray(sums)
            out.writeIntArray(colors)
            out.writeStringArray(categories)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }

}