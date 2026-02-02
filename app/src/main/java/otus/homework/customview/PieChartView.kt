package otus.homework.customview

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
import kotlin.math.atan2
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface OnSliceClickListener {
        fun onCategoryClick(category: String)
    }

    var listener: OnSliceClickListener? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private val colors = listOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA,
        Color.YELLOW, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.BLACK
    )

    private var data: Map<String, Float> = emptyMap()
    private var angles = mutableListOf<Pair<String, Float>>()

    fun setExpenses(expenses: List<Expense>) {
        data = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount.toDouble() }.toFloat() }
        calculateAngles()
        invalidate()
    }

    private fun calculateAngles() {
        angles.clear()
        val total = data.values.sum()
        data.forEach { (cat, value) ->
            angles += cat to (value / total * 360f)
        }
    }

    // --- onMeasure (учтены все MeasureSpec) ---
    override fun onMeasure(w: Int, h: Int) {
        val size = min(
            MeasureSpec.getSize(w),
            MeasureSpec.getSize(h)
        )

        val finalSize = when {
            MeasureSpec.getMode(w) == MeasureSpec.EXACTLY -> MeasureSpec.getSize(w)
            MeasureSpec.getMode(h) == MeasureSpec.EXACTLY -> MeasureSpec.getSize(h)
            else -> size
        }
        setMeasuredDimension(finalSize, finalSize)
    }

    override fun onDraw(canvas: Canvas) {
        var startAngle = -90f
        rect.set(0f, 0f, width.toFloat(), height.toFloat())

        angles.forEachIndexed { i, (cat, sweep) ->
            paint.color = colors[i % colors.size]
            canvas.drawArc(rect, startAngle, sweep, true, paint)
            startAngle += sweep
        }
    }

    // --- обработка клика ---
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_DOWN) return true

        val cx = width / 2f
        val cy = height / 2f
        val angle = (Math.toDegrees(
            atan2(e.y - cy, e.x - cx).toDouble()
        ) + 360 + 90) % 360

        var acc = 0f
        for ((cat, sweep) in angles) {
            acc += sweep
            if (angle <= acc) {
                listener?.onCategoryClick(cat)
                break
            }
        }
        return true
    }

    // --- сохранение состояния ---
    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putSerializable("data", HashMap(data))
        }

    override fun onRestoreInstanceState(state: Parcelable) {
        val b = state as Bundle
        super.onRestoreInstanceState(b.getParcelable("super"))
        data = (b.getSerializable("data") as HashMap<String, Float>)
        calculateAngles()
    }
}
