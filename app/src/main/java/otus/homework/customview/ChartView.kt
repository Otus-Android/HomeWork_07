package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.extensions.getNameByAngle
import otus.homework.customview.pojo.Sector
import otus.homework.customview.util.ChartDefaultData
import kotlin.math.*

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var _isOpened = false
    val isOpened: Boolean get() = _isOpened

    var onSectorClickListener: OnSectorClickListener? = null

    private var centerX = 0f
    private var centerY = 0f
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var pieWidth = 0f

    private var pieDiameter: Float = 0f
    private var pieOuterRadius: Float = 0f
    private var pieInnerRadius: Float = 0f
    private lateinit var outOval: RectF
    private lateinit var innerOval: RectF

    private var textWidth = 0f

    private var charTextStartX = 0f
    private var charTextStartY = 0f
    private var textPlaceRadius = 0f

    private var commentSpaceInYAxis = 0f
    private var commentsPositions = arrayOfNulls<Pair<Float, Float>>(12)
    private val commentRect = RectF()

    private lateinit var _expensesByCategory: Map<String, Int>
    private lateinit var sectorsByCategory: Map<String, Sector>

    private val chartPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.FILL_AND_STROKE
    }

    private val outlinePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val textChartPaint = Paint().apply {
        strokeWidth = 10f
        color = Color.BLACK
        textSize = 50f
        textScaleX = 1.3f
    }.also {
        textWidth = it.measureText("0000")
    }

    fun populate(expensesByCategory: Map<String, Int>) {
        open()
        _expensesByCategory = expensesByCategory
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        if (_expensesByCategory.isNullOrEmpty())  return

        setPieDimensions()
        setCommentsPositions()
        drawPie(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isInsidePie(x, y)) return false

                val categoryName = sectorsByCategory.getNameByAngle(getTouchAngle(x,y))
                onSectorClickListener?.onClick(
                    categoryName,
                    sectorsByCategory[categoryName]!!.color
                )
                close()
                invalidate()
            }
        }
        return true
    }

    private fun setPieDimensions() {
        centerX = width.toFloat()/2
        centerY = height.toFloat()/2

        startX = START_OFFSET
        endX = width.toFloat() - START_OFFSET

        startY = centerY - centerX + START_OFFSET
        endY = centerY  + centerX - START_OFFSET

        pieDiameter = endY - startY
        pieOuterRadius = pieDiameter / 2
        pieWidth = pieDiameter / 6
        pieInnerRadius = pieOuterRadius - pieWidth

        //todo Sectors make in viewmodel
        sectorsByCategory = ChartDefaultData.createSectorsByCategory(_expensesByCategory)
        outOval = RectF(startX, startY, endX, endY)
        innerOval = RectF(startX + pieWidth, startY + pieWidth, endX - pieWidth, endY - pieWidth)

        textPlaceRadius = pieOuterRadius - pieWidth / 2
    }

    private fun setCommentsPositions() {
        commentSpaceInYAxis = (centerY - pieOuterRadius) / 3
        for (i in 0 until _expensesByCategory.size) {
            commentsPositions[i] = when {
                i < 3 ->  Pair(START_OFFSET, i * commentSpaceInYAxis + START_OFFSET)
                i in 3..5 -> Pair(centerX + START_OFFSET, (i - 3) * commentSpaceInYAxis + START_OFFSET)
                i in 6..8 -> Pair(START_OFFSET, endY - commentSpaceInYAxis * (i - 6) + START_OFFSET)
                i in 9..11 -> Pair(centerX + START_OFFSET, endY - commentSpaceInYAxis * (i - 9) + START_OFFSET)
                else -> {null}
            }
        }
    }

    private fun drawPie(canvas: Canvas) {

        var count = 0
        for ((key, value) in sectorsByCategory) {

            chartPaint.color = value.color

            canvas.drawArc(outOval, value.startAngle, value.partPieDegree, true, chartPaint)

            textWidth = textChartPaint.measureText(value.partPiePercentage)
            val a = value.endAngle - (value.partPieDegree / 2)
            charTextStartX = textPlaceRadius * cos((a * PI/180).toFloat()) + centerX
            charTextStartY = textPlaceRadius * sin((a * PI/180).toFloat()) + centerY

            canvas.drawText(value.partPiePercentage, charTextStartX - textWidth / 2, charTextStartY, textChartPaint)

            with(commentsPositions[count]) {
                this?.let {
                    commentRect.apply {
                        left = first
                        top = second - (50f)
                        right = first + 100f
                        bottom = second
                    }
                    canvas.drawRect(commentRect, chartPaint)
                    canvas.drawText(key, first+ TEXT_COMMENT_OFFSET, second, textChartPaint)
                }
            }
            count++
        }
        chartPaint.color = Color.WHITE
        canvas.drawOval(innerOval, chartPaint)
        canvas.drawOval(outOval, outlinePaint)
        canvas.drawOval(innerOval,outlinePaint)
    }

    private fun isInsidePie(x: Float, y: Float): Boolean {
        val touchRadius = hypot((x - centerX).toDouble(), (y - centerY).toDouble())
        return touchRadius in pieInnerRadius..pieOuterRadius
    }

    private fun getTouchAngle(x: Float, y: Float): Float {
        val touchAngle =
            (Math.toDegrees(atan2(y - centerY, x - centerX).toDouble()) + 360) % 360
        return touchAngle.toFloat()
    }

    fun open() {
        if (isOpened) return
        _isOpened = true
        visibility = VISIBLE
    }

    fun close() {
        if (!isOpened) return
        _isOpened = false
        visibility = GONE
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpened = isOpened
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            _isOpened = state.ssIsOpened
            visibility = if (isOpened) VISIBLE else GONE
        }
    }

    companion object {
        const val START_OFFSET = 80f
        const val TEXT_COMMENT_OFFSET = 120f
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpened: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpened = src.readInt() == 1
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpened) 1 else 0)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    interface OnSectorClickListener {
        fun onClick(category: String, color: Int)
    }
}