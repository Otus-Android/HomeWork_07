package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

class GraphView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private enum class Colors(val rgb: Int) {
        GRAY(R.color.grey),
        CYAN(R.color.cyan),
        BLACK(R.color.black),
        RED(R.color.red),
        GREEN(R.color.green),
        MAGENTA(R.color.magenta),
        BLUE(R.color.blue),
        ORANGE(R.color.orange),
        TURQUOISE(R.color.turquoise),
        CUSTOM1(R.color.custom1),
        CUSTOM2(R.color.custom2),
        YELLOW(R.color.yellow)
    }

    private val stroke = convertDpToPixels(STROKE)
    private val strokeGraph = convertDpToPixels(STROKE_GRAPH)
    private val graphPaints = arrayListOf<Paint>()
    private val cornerPathEffect = CornerPathEffect(convertDpToPixels(CORNER_PATH_EFFECT))

    private val paintAxes = Paint().apply {
        color = resources.getColor(Colors.BLACK.rgb, null)
        strokeWidth = stroke
        style = Paint.Style.STROKE
    }

    private var stores: ArrayList<Store> = arrayListOf()

    private val marginAxes = convertDpToPixels(MARGIN_AXES)
    private var defaultWidth = convertDpToPixels(UNSPECIFIED_W).toInt()

    private var countCategory = 0
    private var countDays = 0
    private var maxAmount = 0
    private var minAmount = 0
    private var yStepAmount = 0
    private val vOffset = convertDpToPixels(V_OFFSET)
    private val path = Path()

    private val paintText = Paint().apply {
        color = resources.getColor(R.color.black, null)
        style = Paint.Style.FILL
        textSize = convertSpToPixels(TEXT_SIZE)
        textAlign = Paint.Align.CENTER
        textSkewX = TEXT_SKEW_X
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartViewState(superState, stores)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieChartViewState = state as? PieChartViewState
        super.onRestoreInstanceState(pieChartViewState?.superSavedState ?: state)

        stores = pieChartViewState?.stores ?: arrayListOf()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(
                widthSize,
                heightSize
            )
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth * countCategory,
                heightSize
            )
        }

        when (heightMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(
                widthSize,
                heightSize
            )
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth * countCategory,
                heightSize
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        if (stores.isNotEmpty()) {
            drawAxes(width, height, canvas)
            drawHorizontalNet(width, height, canvas)
            drawVerticalNet(width, height, canvas)
            drawCategories(width, height, canvas)
        }
    }

    private fun drawAxes(width: Int, height: Int, canvas: Canvas) {
        path.reset()
        path.moveTo(marginAxes, marginAxes)
        path.lineTo(marginAxes, height - marginAxes)
        path.lineTo(width - marginAxes, height - marginAxes)
        path.lineTo(width - marginAxes, marginAxes)
        path.lineTo(marginAxes, marginAxes)
        canvas.drawPath(path, paintAxes)
    }

    private fun drawHorizontalNet(width: Int, height: Int, canvas: Canvas) {
        val countLines = maxAmount / yStepAmount + 1
        val yStep = (height - 2 * marginAxes) / countLines

        for (i in 1..countLines) {
            path.reset()
            path.moveTo(marginAxes, marginAxes + yStep * i)
            path.lineTo(width - marginAxes, marginAxes + yStep * i)
            canvas.drawPath(path, paintAxes)
            val widthText = paintText.measureText("${(yStepAmount * (countLines - i))} Р")
            canvas.drawTextOnPath(
                "${(yStepAmount * (countLines - i))} $RUBLE",
                path,
                (width - 2 * marginAxes) / 2 - widthText / DELTA_TEXT,
                vOffset,
                paintText
            )
        }
    }

    private fun drawVerticalNet(width: Int, height: Int, canvas: Canvas) {
        val xStep = (width - 2 * marginAxes) / (countDays + 1)

        canvas.drawText(
            "$ZERO $DAY",
            marginAxes,
            height - marginAxes / 2,
            paintText
        )

        for (i in countDays downTo 1) {
            path.reset()
            path.moveTo(marginAxes + xStep * i, marginAxes)
            path.lineTo(marginAxes + xStep * i, height - marginAxes)
            canvas.drawPath(path, paintAxes)
            canvas.drawText(
                "$i $DAY",
                marginAxes + xStep * i,
                height - marginAxes / 2,
                paintText
            )
        }
    }

    private fun drawCategories(width: Int, height: Int, canvas: Canvas) {
        val xStep = (width - 2 * marginAxes) / (countDays + 1)
        val yStep = (height - 2 * marginAxes) / (maxAmount / yStepAmount)

        stores.sortBy { it.category }

        var j = 0
        stores.groupBy { it.category }.forEach{ (_, categoryStores) ->
            path.reset()
            path.moveTo(marginAxes, height - marginAxes)
            categoryStores.forEachIndexed { index, store ->
                path.lineTo(marginAxes + xStep * (index + 1), height - marginAxes - store.amount.toFloat() / yStepAmount.toFloat() * yStep)
            }
            canvas.drawPath(path, graphPaints[j])
            j++
        }
    }

    fun setStores(newStores: List<Store>) {
        require(newStores.maxOf { it.amount } <= MAX_AMOUNT) { MAX_AMOUNT_EXCEPTION }
        require(newStores.minOf { it.amount } > ZERO) { MAX_AMOUNT_EXCEPTION }

        stores.clear()
        stores.addAll(newStores)

        countCategory = stores.distinctBy { it.category }.size
        countDays = stores.groupingBy { it.category }.eachCount().maxOf { it.value }
        maxAmount = stores.maxOf { it.amount }
        minAmount = stores.minOf { it.amount }
        val delta = maxAmount - minAmount
        yStepAmount = when {
            delta / TEN < ONE -> ONE
            delta / HUNDRED < ONE -> TEN
            delta / THOUSAND < ONE -> HUNDRED
            delta / TEN_THOUSAND < ONE -> THOUSAND
            delta / ONE_HUNDRED_THOUSAND < ONE -> TEN_THOUSAND
            else -> ONE_HUNDRED_THOUSAND
        }

        for (i in 0 until countCategory) {
            Paint().apply {
                color = resources.getColor(Colors.values()[i].rgb, null)
                strokeWidth = strokeGraph
                style = Paint.Style.STROKE
                pathEffect = cornerPathEffect

                graphPaints.add(this)
            }
        }

        requestLayout()
        invalidate()
    }

    companion object {
        private const val ONE = 1
        private const val TEN = 10
        private const val HUNDRED = 100
        private const val THOUSAND = 1000
        private const val TEN_THOUSAND = 10_000
        private const val ONE_HUNDRED_THOUSAND = 100_000
        private const val MAX_AMOUNT = 1_000_000
        private const val MAX_AMOUNT_EXCEPTION =
            "Кол-во трат в день по одной из категорий превышает допустимый $MAX_AMOUNT"
        private const val ZERO = 0
        private const val DAY = "день"
        private const val RUBLE = "Р"
        private const val DELTA_TEXT = 1.5f

        private const val STROKE = 1f
        private const val STROKE_GRAPH = 5f
        private const val CORNER_PATH_EFFECT = 50f
        private const val MARGIN_AXES = 20f
        private const val UNSPECIFIED_W = 256f
        private const val V_OFFSET = -10f
        private const val TEXT_SIZE = 12f
        private const val TEXT_SKEW_X = -0.2f
    }
}