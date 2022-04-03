package otus.homework.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class PieChartView(
    context: Context,
    attributeSet: AttributeSet?
) : View(context, attributeSet) {

    private var categories: List<CategoryOverallSpending> = emptyList()

    private var thickness: Int = 0
    private var colorsList = emptyList<Int>()
    private var defaultSize: Int = 0
    private var percentTextSize: Float = 0f
    private var percentTextOffset: Int = 0
    private val arcRect = RectF()
    private val innerArcRect = RectF()
    private val outerArcRect = RectF()
    private val percentTextRect = Rect()
    private val angles = mutableListOf<Pair<Float, Float>>()
    private val percentTexts = mutableListOf<Pair<String, PointF>>()
    private val sectorsPaths = mutableListOf<Path>()

    private val percentsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clickedSectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val helpPath = Path()
    private var clickedSector: Int? = null
    private var sectorClickListener: OnSectorClickListener? = null

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PieChartView)

        thickness = typedArray.getDimensionPixelSize(R.styleable.PieChartView_thickness, 0)
        defaultSize = context.resources.getDimensionPixelSize(R.dimen.default_chart_size)
        percentTextSize = context.resources.getDimension(R.dimen.percent_text_size)
        percentTextOffset = context.resources.getDimensionPixelOffset(R.dimen.percent_text_offset)

        typedArray.recycle()

        paint.strokeWidth = thickness.toFloat()
        clickedSectorPaint.strokeWidth = thickness.toFloat() * 1.2f
        percentsPaint.apply {
            textSize = percentTextSize
            color = context.resources.getColor(R.color.black, context.theme)
        }
    }

    fun setData(categories: List<CategoryOverallSpending>) {
        this.categories = categories
        configureSectorsColors()
        invalidate()
    }

    private fun configureSectorsColors() {
        colorsList = categories.map { context.resources.getColor(it.category.colorRes, context.theme) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(
            TAG, "onMeasure() called with: " +
                "widthMeasureSpec = ${MeasureSpec.toString(widthMeasureSpec)}, " +
                "heightMeasureSpec = ${MeasureSpec.toString(heightMeasureSpec)}"
        )

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val resultMode = if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) MeasureSpec.EXACTLY
        else MeasureSpec.AT_MOST

        val resultSize: Int = when (resultMode) {
            MeasureSpec.EXACTLY -> {
                min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
            }
            MeasureSpec.AT_MOST,
            MeasureSpec.UNSPECIFIED -> {
                max(
                    (defaultSize + paddingLeft + paddingRight + (percentTextOffset + percentTextRect.width()) * 2),
                    (defaultSize + paddingTop + paddingBottom + (percentTextOffset + percentTextRect.height()) * 2)
                )
            }
            else -> max(
                (defaultSize + paddingLeft + paddingRight + (percentTextOffset + percentTextRect.width()) * 2),
                (defaultSize + paddingTop + paddingBottom + (percentTextOffset + percentTextRect.height()) * 2)
            )
        }
        setMeasuredDimension(resultSize, resultSize)
    }

    private fun calculateArcRect() {
        val diameter = max(
            (measuredWidth - paddingLeft - paddingRight - thickness - (percentTextOffset - percentTextRect.width()) * 2),
            (measuredHeight - paddingTop - paddingBottom - thickness - (percentTextOffset - percentTextRect.height()) * 2)
        )
        val arcRectStartX = (paddingLeft + thickness / 2 + percentTextOffset + percentTextRect.width()).toFloat()
        val arcRectStartY = (paddingTop + thickness / 2 + percentTextOffset + percentTextRect.height()).toFloat()
        innerArcRect.set(
            arcRectStartX + thickness / 2,
            arcRectStartY + thickness / 2,
            diameter + arcRectStartX - thickness / 2,
            diameter + arcRectStartY - thickness / 2
        )
        outerArcRect.set(
            arcRectStartX - thickness / 2,
            arcRectStartY - thickness / 2,
            diameter + arcRectStartX + thickness / 2,
            diameter + arcRectStartY + thickness / 2
        )
        arcRect.set(
            arcRectStartX,
            arcRectStartY,
            diameter + arcRectStartX,
            diameter + arcRectStartY
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateArcRect()
        calculateDrawData(categories.map { it.amount })
        createSectorsPaths()
    }

    private fun calculateDrawData(amounts: List<Int>) {
        angles.clear()
        val amountAll = amounts.sum()
        amounts.fold(START_CIRCLE_ANGLE) { prevAngle, amount ->
            val sector = ((FULL_CIRCLE_ANGLE * amount) / amountAll)
            val newAngle = sector + prevAngle
            angles.add(Pair(prevAngle, newAngle - prevAngle))
            val percent = ((MAX_PERCENT * amount) / amountAll)
            percentTexts.add(
                calculatePercentTextCoordinate(percent, 90 + newAngle - sector / 2)
            )
            newAngle
        }
    }

    private fun calculatePercentTextCoordinate(percent: Float, arcCenterDegree: Float): Pair<String, PointF> {
        val radius = arcRect.width() / 2
        val arcCenterX =
            (arcRect.centerX() + sin(Math.toRadians(arcCenterDegree.toDouble())) * (radius + percentTextOffset)).toFloat()
        val arcCenterY =
            (arcRect.centerY() - cos(Math.toRadians(arcCenterDegree.toDouble())) * (radius + percentTextOffset)).toFloat()

        val percentString = " ${percent.roundToInt()} %"
        percentsPaint.getTextBounds(percentString, 0, percentString.length, percentTextRect)

        val percentTextPointF = PointF()
        if ((arcCenterDegree > 90 && arcCenterDegree <= 180) || arcCenterDegree > FULL_CIRCLE_ANGLE) {
            percentTextPointF.x = arcCenterX
            percentTextPointF.y = arcCenterY + percentTextRect.height() / 2
        } else if (arcCenterDegree > 180 && arcCenterDegree <= FULL_CIRCLE_ANGLE) {
            percentTextPointF.x = arcCenterX - percentTextRect.width()
            percentTextPointF.y = arcCenterY + percentTextRect.height() / 2
        }

        return Pair(percentString, percentTextPointF)
    }

    private fun createSectorsPaths() {
        sectorsPaths.clear()
        angles.forEach { anglesPair ->
            sectorsPaths.add(createSectorPath(anglesPair))
        }
    }

    private fun createSectorPath(anglesPair: Pair<Float, Float>): Path {
        val path1 = Path().apply {
            reset()
            moveTo(arcRect.centerX(), arcRect.centerY())
            addArc(outerArcRect, anglesPair.first, anglesPair.second)
            lineTo(arcRect.centerX(), arcRect.centerY())
        }
        val path2 = Path().apply {
            reset()
            moveTo(arcRect.centerX(), arcRect.centerY())
            addArc(innerArcRect, anglesPair.first, anglesPair.second)
            lineTo(arcRect.centerX(), arcRect.centerY())
        }

        return path1.apply { op(path2, Path.Op.DIFFERENCE) }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            sectorsPaths.forEachIndexed { index, path ->
                drawPath(path, paint.apply { color = colorsList[index] })
            }
            clickedSector?.let {
                drawArc(arcRect, angles[it].first, angles[it].second, false, clickedSectorPaint.apply {
                    color = colorsList[it]
                })
            }
            percentTexts.forEach {
                drawText(it.first, it.second.x, it.second.y, percentsPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val testPath = createHelpPath(event.x, event.y)
            sectorsPaths.forEachIndexed { index, path ->
                testPath.op(path, Path.Op.DIFFERENCE)
                // если сектор перекрывает тестовый квадрат, резалт path пустой
                if (testPath.isEmpty) {
                    Log.d(
                        TAG,
                        "sector of ${categories[index].category} with overall spending ${categories[index].amount} was clicked"
                    )
                    clickedSector = index
                    invalidate()
                    sectorClickListener?.onSectorSelect(categories[index])
                    return true
                }
            }
            // клик не по сектору
            clickedSector = null
            invalidate()
        }
        return super.onTouchEvent(event)
    }

    private fun createHelpPath(x: Float, y: Float): Path {
        helpPath.apply {
            reset()
            moveTo(x, y)
            val helpRect = RectF(x - 0.5f, y - 0.5f, x + 0.5f, y + 0.5f)
            addRect(helpRect, Path.Direction.CW)
        }

        return helpPath
    }

    fun setSectorClickListener(listener: OnSectorClickListener) {
        sectorClickListener = listener
    }

    interface OnSectorClickListener {
        fun onSectorSelect(category: CategoryOverallSpending)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.selectedSector = clickedSector
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(state.superState)
        clickedSector = savedState.selectedSector
        invalidate()
    }

    private class SavedState : BaseSavedState {

        var selectedSector: Int? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedSector = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            selectedSector?.let { parcel.writeInt(it) }
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    private companion object {
        const val TAG = "PieChartView"
        const val START_CIRCLE_ANGLE = 0f
        const val FULL_CIRCLE_ANGLE = 360f
        const val MAX_PERCENT = 100f
    }
}