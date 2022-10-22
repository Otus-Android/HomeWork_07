package otus.homework.customview.ui

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.entities.AmountLine
import otus.homework.customview.entities.Spending
import otus.homework.customview.entities.TimeLine
import otus.homework.customview.tools.Time
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

class TimelineView : View {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val spendingList: MutableList<Spending> = mutableListOf()

    private val pointList: MutableList<PointF> = mutableListOf()
    private val rectForDraw = RectF()
    private val amountLines: MutableList<AmountLine> = mutableListOf()
    private val timeLines: MutableList<TimeLine> = mutableListOf()

    private val timeMapper = Time()
    private val path = Path()

    private val pathPaint = Paint().apply {
        pathEffect = CornerPathEffect(mapDpInPixels(10f))
        style = Paint.Style.STROKE
        strokeWidth = mapDpInPixels(5f)
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = mapDpInPixels(1f)
        pathEffect =
            DashPathEffect(floatArrayOf(mapDpInPixels(10f), mapDpInPixels(5f)), mapDpInPixels(15f))
    }
    private val timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(37, 150, 190)
        textSize = mapSpInPixels(14f)
        textAlign = Paint.Align.CENTER
    }
    private val amountTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(37, 150, 190)
        textSize = mapSpInPixels(14f)
        textAlign = Paint.Align.LEFT
    }

    override fun onSaveInstanceState(): Parcelable =
        TimelineViewSavedState(pathPaint.color, super.onSaveInstanceState())

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is TimelineViewSavedState) {
            super.onRestoreInstanceState(state.superState)
            initCategoryColor(state.categoryColor)
        } else super.onRestoreInstanceState(state)
    }

    fun updateSpendingList(list: List<Spending>) {
        spendingList.clear()
        spendingList.addAll(list)
    }

    fun initCategoryColor(@ColorInt categoryColor: Int) {
        pathPaint.color = categoryColor
        pointPaint.color = categoryColor
    }

    private fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

    private fun initRectForDraw() {
        rectForDraw.left = mapDpInPixels(HORIZONTAL_PADDING)
        rectForDraw.right = width - mapDpInPixels(HORIZONTAL_PADDING)
        rectForDraw.top = mapDpInPixels(VERTICAL_PADDING)
        rectForDraw.bottom = height - mapDpInPixels(VERTICAL_PADDING)
    }

    private fun mapDpInPixels(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    private fun mapSpInPixels(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)

    private fun listWithSumSpendingByDate(list: List<Spending>): List<Spending> {
        val set = list.map { Spending(time = it.time) }.toMutableSet()
        set.forEach { setItem ->
            setItem.amount = list
                .filter { it.time == setItem.time }
                .map { it.amount }
                .fold(0) { total, item -> total + item }
        }
        if (set.size == 1) set.add(Spending(time = set.elementAt(0).time - SECONDS_IN_DAY))
        return set.toList()
    }

    private fun initGraphField() {
        if (spendingList.size == 0) return
        val listWithCorrectDate =
            spendingList.map { it.copy(time = timeMapper.timeToDateInSeconds(it.time)) }
        val listWithSumSpendingByDate = listWithSumSpendingByDate(listWithCorrectDate)
        var minTime = listWithSumSpendingByDate[0].time
        var maxTime = listWithSumSpendingByDate[0].time
        var minAmount = listWithSumSpendingByDate[0].amount
        var maxAmount = listWithSumSpendingByDate[0].amount
        listWithSumSpendingByDate.forEach {
            if (it.time < minTime) minTime = it.time
            if (it.time > maxTime) maxTime = it.time
            if (it.amount < minAmount) minAmount = it.amount
            if (it.amount > maxAmount) maxAmount = it.amount
        }
        val minDayInSeconds = timeMapper.timeToDateInSeconds(minTime)
        val maxDayInSeconds = timeMapper.timeToDateInSeconds(maxTime)
        val daysNumber = (maxDayInSeconds - minDayInSeconds) / SECONDS_IN_DAY
        val horizontalGridStepInDays = ceil(daysNumber.toFloat() / TIME_CELLS_NUMBER)
        val verticalDesiredStep = maxAmount / LINES_NUMBER
        val baseVerticalGridStep = 10f.pow(verticalDesiredStep.length() - 1)
        val verticalGridStep =
            ceil((verticalDesiredStep / baseVerticalGridStep)) * baseVerticalGridStep
        val resultAmountInPixels =
            (rectForDraw.height()) / (verticalGridStep * LINES_NUMBER)
        for (i in 0..LINES_NUMBER) {
            amountLines
                .add(
                    AmountLine(
                        rectForDraw.bottom - i * (rectForDraw.height()) / LINES_NUMBER,
                        (i * verticalGridStep.toInt()).toString()
                    )
                )
        }
        val numberVerticalLines = ceil(daysNumber / horizontalGridStepInDays).toInt()
        val timeInPixels =
            (rectForDraw.width()) / (horizontalGridStepInDays * numberVerticalLines * SECONDS_IN_DAY)
        for (i in 0..numberVerticalLines) {
            timeLines
                .add(
                    TimeLine(
                        rectForDraw.left + i * (rectForDraw.width()) / numberVerticalLines,
                        timeMapper.timeToDayAndMonthString(minTime + i * SECONDS_IN_DAY * horizontalGridStepInDays.toInt())
                    )
                )
        }
        listWithSumSpendingByDate.forEach {
            val x = rectForDraw.left + (it.time - minTime) * timeInPixels
            val y = rectForDraw.bottom - it.amount * resultAmountInPixels
            pointList.add(PointF(x, y))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paddingWidth = paddingStart + paddingEnd
        val paddingHeight = paddingTop + paddingBottom
        val suggestedWidth = suggestedMinimumWidth + paddingWidth
        val suggestedHeight = suggestedMinimumHeight + paddingHeight
        setMeasuredDimension(
            resolveSize(suggestedWidth, widthMeasureSpec),
            resolveSize(suggestedHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initRectForDraw()
        initGraphField()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || pointList.size == 0) return
        drawGrid(canvas)
        drawPath(canvas)
        drawPoints(canvas)
    }

    private fun drawPath(canvas: Canvas) {
        path.reset()
        path.moveTo(pointList[0].x, pointList[0].y)
        pointList.forEach { path.lineTo(it.x, it.y) }
        canvas.drawPath(path, pathPaint)
    }

    private fun drawGrid(canvas: Canvas) {
        amountLines.forEach { amountLine ->
            canvas.drawLine(
                rectForDraw.left,
                amountLine.y,
                rectForDraw.right,
                amountLine.y,
                gridPaint
            )
            canvas.drawText(
                amountLine.text, rectForDraw.left + mapDpInPixels(
                    AMOUNT_TEXT_LEFT_PADDING
                ), amountLine.y - timeTextPaint.descent(), amountTextPaint
            )
        }
        timeLines.forEach { timeLine ->
            canvas.drawLine(timeLine.x, rectForDraw.top, timeLine.x, rectForDraw.bottom, gridPaint)
            canvas.drawText(
                timeLine.text,
                timeLine.x,
                rectForDraw.bottom + timeTextPaint.descent() - timeTextPaint.ascent(),
                timeTextPaint
            )
        }
    }

    private fun drawPoints(canvas: Canvas) {
        pointList.forEach { canvas.drawCircle(it.x, it.y, mapDpInPixels(5f), pointPaint) }
    }

    companion object {

        private const val HORIZONTAL_PADDING = 40f
        private const val VERTICAL_PADDING = 40f
        private const val LINES_NUMBER = 5
        private const val TIME_CELLS_NUMBER = 5
        private const val SECONDS_IN_DAY = 24 * 60 * 60
        private const val AMOUNT_TEXT_LEFT_PADDING = 5f
    }
}