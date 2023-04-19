package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.models.Spend

class LineChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var minChartSizeX = 400.dpToPx(context.resources)
    private var minChartSizeY = 400.dpToPx(context.resources)

    private var groupedItems: Map<String, List<Spend>> = emptyMap()

    private var rectF: RectF = RectF()
    private val strokeSize = 16.dpToPx(context.resources).toFloat()

    private val axisPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = strokeSize
    }
    private val linePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = strokeSize / 4
    }
    private val dotPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL_AND_STROKE
        color = Color.BLUE
        strokeWidth = strokeSize / 2
    }

    private var selectedIndex = -1

    fun setItems(list: Map<String, List<Spend>>) {
        groupedItems = list
    }

    fun setSelectedIndex(i: Int){
        selectedIndex = i
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val chartWidth =
            Integer.max(minChartSizeX, suggestedMinimumWidth + paddingLeft + paddingRight)
        val chartHeight =
            Integer.max(minChartSizeY, suggestedMinimumHeight + paddingTop + paddingBottom)

        val sizeX = resolveSize(chartWidth, widthMeasureSpec)
        val sizeY = resolveSize(chartHeight, heightMeasureSpec)

        setMeasuredDimension(sizeX, sizeY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startTop = 0f
        val startLeft = 0f
        val endBottom = height.toFloat()
        val rightBottom = height.toFloat()
        rectF.set(
            startLeft + strokeSize, startTop, endBottom,
            rightBottom - strokeSize
        )
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), axisPaint)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint)
        if (selectedIndex != -1 && groupedItems.isNotEmpty()) {
            val key = groupedItems.keys.toList()[selectedIndex]
            val item = groupedItems[key]
            if (item?.isNotEmpty() == true) {
                val maxTime = item.maxBy{ it.time }
                val maxAmount = item.maxBy { it.amount }
                var startX = 0f
                var startY = height.toFloat()
                item.forEach {
                    val pxX =  width.toFloat()*it.time/maxTime.time
                    val pxY = height.toFloat()*it.amount/maxAmount.amount
                    canvas.drawLine(startX, startY, pxX, pxY, linePaint)
                    canvas.drawPoint(pxX, pxY, dotPaint)
                    startX = pxX
                    startY = pxY
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val savedState = SavedState(superState)

        savedState.selectedIndex = selectedIndex
        savedState.groupedItems = groupedItems

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        groupedItems = state.groupedItems
        selectedIndex = state.selectedIndex
        invalidate()
    }

    internal class SavedState : BaseSavedState {
        var groupedItems: Map<String, List<Spend>> = emptyMap()
        var selectedIndex: Int = -1

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            groupedItems = emptyMap()
            parcel.readMap(groupedItems, Map::class.java.classLoader)
            selectedIndex = parcel.readInt()

        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeMap(groupedItems)
            out.writeInt(selectedIndex)
        }
    }
}