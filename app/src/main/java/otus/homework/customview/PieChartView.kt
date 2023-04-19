package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.models.PieSlice
import otus.homework.customview.models.Spend
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var minPieChartSize = 200.dpToPx(context.resources)

    private var rectF: RectF = RectF()

    private var strokeSize = 16.dpToPx(context.resources).toFloat()
    private var clickedIndex = -1

    private val slicePaint: Paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = strokeSize
    }

    private val textPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = strokeSize
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private var weightsAndColors: ArrayList<PieSlice> = arrayListOf()
    private var items = emptyList<Spend>()

    private val outerRect = RectF()
    private val innerRect = RectF()

    fun setItems(list: List<Spend>) {
        items = list
        initWeightsAndColors()
    }

    private fun initWeightsAndColors() {
        clearChart()
        val groupedList = items.groupBy { it.category }
        val total = items.fold(0) { acc, item -> acc.plus(item.amount) }
        val colors = ColorGenerator.generatePalette(items.size, Color.BLUE, false)
        val weights = groupedList.map { setItem ->
            setItem.value.fold(0) { acc, item -> acc.plus(item.amount) }.times(100f).div(total)
        }
        val totalAmounts = groupedList.map { setItem ->
            setItem.value.fold(0) { acc, item -> acc.plus(item.amount) }
        }
        weights.forEachIndexed { i, item ->
            weightsAndColors.add(PieSlice(item, colors[i], totalAmount = totalAmounts[i]))
        }
    }

    private fun clearChart() {
        weightsAndColors.clear()
        invalidate()
    }

    private val generalGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                return handleViewClick(event)
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return generalGestureDetector.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val chartWidth =
            Integer.max(minPieChartSize, suggestedMinimumWidth + paddingLeft + paddingRight)
        val chartHeight =
            Integer.max(minPieChartSize, suggestedMinimumHeight + paddingTop + paddingBottom)

        val size = Integer.min(
            resolveSize(chartWidth, widthMeasureSpec),
            resolveSize(chartHeight, heightMeasureSpec)
        )

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val startTop = 0f
        val startLeft = 0f
        val endBottom = height.toFloat()
        val rightBottom = height.toFloat()
        rectF.set(
            startLeft+strokeSize, startTop+strokeSize, endBottom-strokeSize,
            rightBottom-strokeSize
        )
        outerRect.set(
            startLeft,
            startTop,
            endBottom,
            rightBottom
        )
        innerRect.set(rectF)
        innerRect.inset(width *0.1f, height *0.1f)

        if (weightsAndColors.size > 0) {
            val scaledValues = scale()
            var sliceStartPoint = 0f//(width / 2).toFloat()
            for (i in 0 until weightsAndColors.size) {
                weightsAndColors[i].fromAngle = sliceStartPoint
                weightsAndColors[i].toAngle = scaledValues[i]
                slicePaint.color =
                    weightsAndColors[i].color
                canvas.drawArc(
                    rectF,
                    sliceStartPoint,
                    scaledValues[i],
                    false,
                    slicePaint.apply {
                        strokeWidth = if(clickedIndex == i){
                            strokeSize+(strokeSize/2)
                        }else{
                            strokeSize
                        }
                    }
                )
                if (clickedIndex == i){
                    canvas.drawText(weightsAndColors[i].totalAmount.toString(), width/2f, width/2f, textPaint)
                }
                sliceStartPoint += scaledValues[i]
            }

        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val savedState = SavedState(superState)

        savedState.selectedIndex = clickedIndex
        savedState.itemsList = items

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        items = state.itemsList
        clickedIndex = state.selectedIndex
        initWeightsAndColors()
    }

    private fun scale(): FloatArray {
        val scaledValues = FloatArray(weightsAndColors.size)
        for (i in weightsAndColors.indices) {
            scaledValues[i] =
                (weightsAndColors[i].weight * 360f / 100f)
        }
        return scaledValues
    }

    private fun handleViewClick(event: MotionEvent): Boolean {
        val innerRadius = innerRect.width() / 2f
        val outerRadius = outerRect.width() / 2f

        val zeroX = event.x - innerRect.centerX()
        val zeroY = -(event.y - innerRect.centerY())

        val eventRadius = hypot(zeroX, zeroY)

        return if (eventRadius in innerRadius..outerRadius) {
            val eventAngleRad = atan2(zeroY, zeroX).let {
                if (it < 0) abs(it) else 2 * PI - it
            }.toDouble()

            val eventAngleDegrees = Math.toDegrees(eventAngleRad)
            weightsAndColors.forEachIndexed{index, item ->
                if (item.isIn(eventAngleDegrees)){
                    clickedIndex = index
                    invalidate()
                }
            }
            true
        } else {
            false
        }
    }

    internal class SavedState : BaseSavedState {
        var itemsList: List<Spend> = arrayListOf()
        var selectedIndex: Int = -1

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            itemsList = listOf()
            parcel.readList(itemsList, List::class.java.classLoader)
            selectedIndex = parcel.readInt()

        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(itemsList)
            out.writeInt(selectedIndex)
        }
    }
}