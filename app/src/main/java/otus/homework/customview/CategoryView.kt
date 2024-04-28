package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlinx.parcelize.Parcelize
import kotlin.math.min

class CategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var spendings: List<Spending> = emptyList()
    private var circles: MutableList<PointF> = mutableListOf()
    private var dates: MutableList<String> = mutableListOf()
    private var axisRect: RectF = RectF()
    private var graphRect: RectF = RectF()
    private var maxValue = 0f

    private val graphColor = Color.parseColor("#519DE9")
    private val path = Path()
    private val dateHeight = 30

    private val paintGraph = Paint().apply {
        color = graphColor
        strokeWidth = 5f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(5f)
    }

    private val paintDot = Paint().apply {
        color = graphColor
        style = Paint.Style.FILL_AND_STROKE
    }

    private val paintAxis = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val paintDash = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(2f, 4f), 2f)
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
        textSize = 26f
    }

    init {
        if (isInEditMode) {
            setup(
                listOf(
                    Spending("10 сен.", 1580),
                    Spending("12 сен.", 129),
                    Spending("02 окт.", 809),
                    Spending("04 окт.", 206),
                    Spending("10 окт.", 1000),
                )
            )
        }
    }

    fun setup(spendings: List<Spending>) {
        this.spendings = spendings
        maxValue = spendings.maxOf { it.amount.toFloat() }
        recalculate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = 500
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(defaultSize, wSize)
            else -> defaultSize
        }
        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(defaultSize, hSize)
            else -> defaultSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        axisRect = RectF(
            0f + paddingLeft,
            0f + paddingTop,
            w.toFloat() - paddingRight,
            h.toFloat() - paddingBottom - dateHeight
        )
        graphRect = RectF(
            axisRect.left,
            axisRect.bottom - (axisRect.bottom / 100 * 90), // справа и сверху график на 10% меньше чем ось
            axisRect.right - (axisRect.right / 100 * 10),
            axisRect.bottom
        )
        recalculate()
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
            putParcelable(DATA_STATE_KEY, Save(
                spendings = spendings,
                circles = circles,
                dates = dates,
                axisRect = axisRect,
                graphRect = graphRect,
                maxValue = maxValue
            ))
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        bundle.getParcelable<Save>(DATA_STATE_KEY)?.let { saved ->
            spendings = saved.spendings
            circles = saved.circles.toMutableList()
            dates = saved.dates.toMutableList()
            axisRect = saved.axisRect
            graphRect = saved.graphRect
            maxValue = saved.maxValue
        }
        super.onRestoreInstanceState(bundle.getParcelable(SUPER_STATE_KEY))
    }

    override fun onDraw(canvas: Canvas) {
        drawGraph(canvas)
        drawAxis(canvas)
    }

    private fun drawGraph(canvas: Canvas) {
        canvas.drawPath(path, paintGraph)
        circles.forEachIndexed { index, point ->
            canvas.drawLine(point.x, point.y, point.x, graphRect.bottom, paintDash)
            canvas.drawLine(point.x, point.y, graphRect.left, point.y, paintDash)
            canvas.drawCircle(point.x, point.y, 8f, paintDot)
            canvas.drawText(spendings[index].amount.toString(), graphRect.left + 10, point.y - 5, paintText)
            val dateWidth = paintText.measureText(dates[index])
            canvas.drawText(dates[index], point.x - dateWidth / 2, graphRect.bottom + dateHeight, paintText)
        }
    }

    private fun drawAxis(canvas: Canvas) {
        with(axisRect) {
            canvas.drawLine(left, top, left, bottom, paintAxis)
            canvas.drawLine(left, bottom, right, bottom, paintAxis)
        }
    }

    private fun recalculate() {
        if (spendings.isEmpty()) return
        circles.clear()
        dates.clear()
        path.reset()

        val wStep = graphRect.right / spendings.size + 1
        val hStep = (graphRect.bottom - graphRect.top) / maxValue
        path.moveTo(graphRect.left, graphRect.bottom)
        var x = 0f + wStep

        for (element in spendings) {
            val y = graphRect.bottom - ((element.amount.toDouble()) * hStep).toFloat()
            path.lineTo(x, y)
            circles.add(PointF(x, y))
            x += wStep
            dates.add(element.date)
        }
        invalidate()
    }

    @Parcelize
    data class Spending(
        val date: String,
        val amount: Int,
    ): Parcelable

    @Parcelize
    private data class Save(
        val spendings: List<Spending>,
        val circles: List<PointF>,
        val dates: List<String>,
        val axisRect: RectF,
        val graphRect: RectF,
        val maxValue: Float,
    ): Parcelable

    companion object {
        private const val DATA_STATE_KEY = "DATA_STATE_KEY"
        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"
    }
}