package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val colors = arrayOf(
            Color.parseColor("#ff0000"),
            Color.parseColor("#00ff00"),
            Color.parseColor("#0000ff"),
            Color.parseColor("#ffff00"),
            Color.parseColor("#00ffff"),
            Color.parseColor("#ff00ff"),
            Color.parseColor("#f0000f"),
            Color.parseColor("#0f00f0"),
            Color.parseColor("#ff0ff0"),
            Color.parseColor("#0ff0ff")
        )
    }

    private var _items: List<Item> = emptyList()
    var items: List<Item>
        get() = _items
        set(value) {
            _items = value
            _totalAmount = _items.sumOf { it.amount }
            paths = _items.map { Path() }
            requestLayout()
            invalidate()
        }

    private var _onItemTouch: ((Item) -> Unit)? = null
    var onItemTouch: ((Item) -> Unit)?
        get() = _onItemTouch
        set(value) {
            _onItemTouch = value
            isClickable = value != null
        }

    private var _totalAmount = 0

    private var paths = listOf<Path>()

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var horizontalOffset = 0f
    private var verticalOffset = 0f
    private val chartRect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var startAngle = 0f
    private var sweepAngle = 0f

    init {
        if (isInEditMode) {
            items = listOf(
                Item("Item 1", 10),
                Item("Item 2", 30),
                Item("Item 3", 20),
                Item("Item 4", 40),
                Item("Item 5", 50)
            )
        }
    }

    private fun handleTouch(event: MotionEvent) {
        val onItemTouch = this.onItemTouch ?: return

        val rect = RectF()
        val region = Region()

        items.forEachIndexed { index, item ->
            paths[index].computeBounds(rect, false)

            region.setPath(
                paths[index],
                Region(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            )

            if (region.contains(event.x.toInt(), event.y.toInt())) {
                onItemTouch(item)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            handleTouch(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = height
            }
            MeasureSpec.EXACTLY -> { /* leave exactly width */ }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = width
            }
            MeasureSpec.EXACTLY -> { /* leave exactly height */ }
        }

        horizontalOffset = if (height > width) 0f else (width - height).toFloat() / 2
        verticalOffset = if (width > height) 0f else (height - width).toFloat() / 2
        chartRect.left = horizontalOffset + paddingLeft
        chartRect.top = verticalOffset + paddingTop
        chartRect.right = width - horizontalOffset - paddingRight
        chartRect.bottom = height - verticalOffset - paddingBottom
        centerX = width.toFloat() / 2
        centerY = height.toFloat() / 2

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        startAngle = -90f
        items.forEachIndexed { index, item ->
            sweepAngle = (item.amount.toFloat() / _totalAmount) * 360
            paint.color = colors[index % 10]
            paths[index].arcTo(
                chartRect,
                startAngle,
                sweepAngle,
                true
            )
            paths[index].lineTo(centerX, centerY)
            paths[index].close()

            canvas?.drawPath(paths[index], paint)
            startAngle += sweepAngle
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            items = this@PieChart.items
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        items = state.items
    }

    class SavedState: BaseSavedState {
        var items: List<Item> = emptyList()

        constructor(superState: Parcelable?) : super(superState)
        private constructor(source: Parcel?) : super(source) {

        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)

            out?.writeList(items)
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Item(
        val label: String,
        val amount: Int
    )
}