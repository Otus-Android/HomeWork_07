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
import kotlin.math.sqrt

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var onSliceClick: ((String) -> Unit)? = null
): View(context, attrs) {

    val Int.dp: Float
        get() = this * resources.displayMetrics.density

    private var data: Map<String, List<CategoryModel>> = emptyMap()
    private val sectors = mutableListOf<Sector>()
    private lateinit var rect: RectF

    fun setData(items: Map<String, List<CategoryModel>>) {
        data = items
        calculateSectors()
        invalidate()
    }

    init{
        if (isInEditMode) {
            //setValues(listOf(1,2,3,4,5))
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val bundle = Bundle()
        bundle.putParcelable("super_state", superState)
        bundle.putSerializable(STATE_DATE, HashMap(data))
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState = state.getParcelable<Parcelable>("super_state")
            super.onRestoreInstanceState(superState)

            data = state.getSerializable("data") as HashMap<String, List<CategoryModel>>
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val size = minOf(w, h).toFloat()

        val left = (w - size) / 2f
        val top = (h - size) / 2f

        rect = RectF(left, top, left + size, top + size)
    }

    val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (sectors.isEmpty()) return

        sectors.forEach {
            paint.color = randomColor()

            canvas.drawArc(
                rect,
                it.start,
                it.end - it.start,
                true,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_DOWN) return true

        val dx = event.x - rect.centerX()
        val dy = event.y - rect.centerY()

        val distance = sqrt(dx * dx + dy * dy)

        if (distance > rect.width() / 2f) return true

        val angle = ((Math.toDegrees(
            atan2(dy.toDouble(), dx.toDouble())
        ) + 360) % 360).toFloat()

        sectors.forEach { sector ->
            if (angle in sector.start..sector.end) {
                onSliceClick?.invoke(sector.category)
                return true
            }
        }
        return true
    }

    private fun randomColor(): Int {
        return Color.rgb(
            (0..255).random(),
            (0..255).random(),
            (0..255).random(),
        )
    }

    private fun calculateSectors() {
        sectors.clear()

        val total = data.values.sumOf { list ->
            list.sumOf { it.amount }
        }.toFloat()

        var startAngle = 0f

        data.forEach { (category, items) ->
            val amount = items.sumOf { it.amount }.toFloat()

            val sweep =
                if (amount != 0f) {
                    (amount / total) * 360f
                } else 0f

            sectors.add(
                Sector(
                    category = category,
                    start = startAngle,
                    end = startAngle + sweep
                )
            )

            startAngle += sweep
        }
    }

    companion object {
        private const val STATE_DATE = "data"
    }
}