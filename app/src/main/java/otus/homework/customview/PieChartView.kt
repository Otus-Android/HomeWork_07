package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.core.graphics.withSave

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnSectorClickListener {
        fun onSectorClick(categoryData: CategoryData)
    }

    private var onSectorClickListener: OnSectorClickListener? = null

    fun setOnSectorClickListener(listener: OnSectorClickListener) {
        this.onSectorClickListener = listener
    }

    private var categoriesData = mutableListOf<CategoryData>()
    private var totalAmount = 0.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val legendPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var selectedSector = -1
    private var textSize = 0f
    private var legendTextSize: Int = 0

    init {
        setupPaints()
    }

    private fun setupPaints() {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f

        textPaint.color = Color.BLACK
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        legendPaint.color = Color.DKGRAY
        legendPaint.textAlign = Paint.Align.LEFT
        legendPaint.textSize = 36f

        selectedPaint.style = Paint.Style.STROKE
        selectedPaint.color = Color.BLACK
        selectedPaint.strokeWidth = 4f

        centerPaint.color = Color.WHITE
        centerPaint.style = Paint.Style.FILL
    }

    fun setData(categories: List<CategoryData>) {
        categoriesData.clear()
        categoriesData.addAll(categories)
        totalAmount = categoriesData.sumOf { it.amount }
        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSize = dpToPx(400)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(widthSize, minSize)
            else -> minSize
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(heightSize, minSize)
            else -> minSize
        }

        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) * 0.3f

        textSize = radius * 0.1f
        legendTextSize = dpToPx(12)

        textPaint.textSize = textSize
        legendPaint.textSize = legendTextSize.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (categoriesData.isEmpty() || totalAmount == 0.0) {
            drawEmptyState(canvas)
            return
        }

        var startAngle = -90f

        for ((index, category) in categoriesData.withIndex()) {
            val sweepAngle = (category.amount / totalAmount * 360).toFloat()

            paint.color = category.color

            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle,
                sweepAngle,
                true,
                paint
            )

            if (index == selectedSector) {
                canvas.drawArc(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius,
                    startAngle,
                    sweepAngle,
                    true,
                    selectedPaint
                )
            }

            if (sweepAngle > 10) {
                val angle = startAngle + sweepAngle / 2
                val textRadius = radius * 0.6f
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * textRadius
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * textRadius

                val displayText = if (category.category.length > 10) {
                    "${category.category.substring(0, 8)}..."
                } else {
                    category.category
                }

                canvas.withSave {
                    if (angle > 90 && angle < 270) {
                        rotate(angle + 180, x, y)
                        drawText(displayText, x, y, textPaint)
                    } else {
                        rotate(angle, x, y)
                        drawText(displayText, x, y, textPaint)
                    }
                }
            }

            startAngle += sweepAngle
        }

        canvas.drawCircle(centerX, centerY, radius * 0.2f, centerPaint)

        val totalText = "Всего:\n${String.format("%.0f", totalAmount)} руб."
        val totalBounds = Rect()
        textPaint.getTextBounds(totalText, 0, totalText.length, totalBounds)

        canvas.drawText(
            totalText,
            centerX,
            centerY - textPaint.descent(),
            textPaint.apply { color = Color.BLACK }
        )
    }

    private fun drawEmptyState(canvas: Canvas) {
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, radius, paint)

        textPaint.color = Color.DKGRAY
        canvas.drawText(
            "Нет данных",
            centerX,
            centerY,
            textPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                val distance = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))

                if (distance <= radius) {
                    var angle =
                        Math.toDegrees(atan2((y - centerY).toDouble(), (x - centerX).toDouble()))
                    angle = (angle + 360) % 360
                    angle = (angle + 90) % 360

                    var currentAngle = 0f
                    for ((index, category) in categoriesData.withIndex()) {
                        val sweepAngle = (category.amount / totalAmount * 360).toFloat()

                        if (angle >= currentAngle && angle < currentAngle + sweepAngle) {
                            selectedSector = index
                            onSectorClickListener?.onSectorClick(category)
                            invalidate()
                            return true
                        }
                        currentAngle += sweepAngle
                    }
                }
                selectedSector = -1
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            this.selectedSector = this@PieChartView.selectedSector
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        if (savedState != null) {
            super.onRestoreInstanceState(savedState.superState)
            selectedSector = savedState.selectedSector
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        var selectedSector: Int = -1

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedSector = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedSector)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
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