package otus.homework.customview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import otus.homework.customview.entities.Spending
import otus.homework.customview.entities.TimelineGrid
import otus.homework.customview.tools.Time

class TimelineView : View {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val spendingList: MutableList<Spending> = mutableListOf()

    private val pointList: MutableList<PointF> = mutableListOf()
    private val animatedPointList: MutableList<PointF> = mutableListOf()
    private val rectForDraw = RectF()
    private lateinit var timelineGrid: TimelineGrid

    private val timeMapper = Time()
    private val path = Path()
    private val animatedPath = Path()

    private val pathPaint = Paint().apply {
        pathEffect = CornerPathEffect(mapDpInPixels(5f))
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

    fun initView(list: List<Spending>) {
        spendingList.clear()
        spendingList.addAll(list)
    }

    fun initCategoryColor(@ColorInt categoryColor: Int) {
        pathPaint.color = categoryColor
        pointPaint.color = categoryColor
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
        if (set.size == 1) set.add(Spending(time = set.elementAt(0).time - timeMapper.secondsInDay()))
        return set.toList().sortedBy { it.time }
    }

    private fun initGraphField() {
        if (spendingList.size == 0) return
        val listWithCorrectDate =
            spendingList.map { it.copy(time = timeMapper.timeToDateInSeconds(it.time)) }
        val listWithSumSpendingByDate = listWithSumSpendingByDate(listWithCorrectDate)
        timelineGrid = TimelineGrid(listWithSumSpendingByDate, timeMapper, rectForDraw)
        listWithSumSpendingByDate.forEach {
            val x = rectForDraw.left + (it.time - timelineGrid.minTime) * timelineGrid.timeInPixels
            val y = rectForDraw.bottom - it.amount * timelineGrid.amountInPixels
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
        initPath()
        runAnimation()
    }

    private fun initPath() {
        path.reset()
        path.moveTo(pointList[0].x, pointList[0].y)
        pointList.forEach { path.lineTo(it.x, it.y) }
    }

    fun runAnimation() {
        if (pointList.size == 0) return
        val pathMeasure = PathMeasure(path, false)
        var currentIndexPoint = 0
        animatedPointList.clear()
        animatedPath.reset()
        animatedPath.moveTo(pointList[0].x, pointList[0].y)
        val tan = floatArrayOf(0f, 0f)
        var currentTan = floatArrayOf(0f, 0f)
        val pos = floatArrayOf(0f, 0f)
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (pathMeasure.length).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val distance = it.animatedValue as Float
                pathMeasure.getPosTan(distance * pathMeasure.length, pos, tan)
                if (!tan.contentEquals(currentTan)) {
                    currentTan = tan.clone()
                    animatedPointList.add(pointList[currentIndexPoint++])
                }
                val x = pos[0]
                val y = pos[1]
                animatedPath.lineTo(x, y)
                invalidate()
            }
            start()
            doOnEnd { addLastPointToAnimatedList() }
        }
    }

    private fun addLastPointToAnimatedList() {
        animatedPointList.add(pointList.last())
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || pointList.size == 0) return
        drawGrid(canvas)
        drawGraph(canvas)
        drawPoints(canvas)
    }

    private fun drawGraph(canvas: Canvas) {
        canvas.drawPath(animatedPath, pathPaint)
    }

    private fun drawGrid(canvas: Canvas) {
        timelineGrid.amountLines.forEach { amountLine ->
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
        timelineGrid.timeLines.forEach { timeLine ->
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
        animatedPointList.forEach { canvas.drawCircle(it.x, it.y, mapDpInPixels(5f), pointPaint) }
    }

    companion object {

        private const val HORIZONTAL_PADDING = 40f
        private const val VERTICAL_PADDING = 40f
        private const val AMOUNT_TEXT_LEFT_PADDING = 5f
    }
}