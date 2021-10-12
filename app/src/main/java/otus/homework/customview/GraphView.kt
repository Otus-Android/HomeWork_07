package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
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

    private val stroke = 1f
    private val pieChartPaints = arrayListOf<Paint>()

    private val paintAxes = Paint().apply {
        color = resources.getColor(Colors.BLACK.rgb, null)
        strokeWidth = stroke
        style = Paint.Style.STROKE
    }

    private var stores: ArrayList<Store> = arrayListOf()

    private val unspecifiedW = 256
    private val unspecifiedH = 256

    private val marginAxes = (context.resources.displayMetrics.density * 20f)
    private var defaultWidth = (context.resources.displayMetrics.density * unspecifiedW).toInt()

    private var countCategory = 0
    private var countDays = 0
    private var maxAmount = 0
    private var minAmount = 0
    private var yStepAmount = 0
    private val path = Path()

    private val paintText = Paint().apply {
        color = resources.getColor(R.color.black, null)
        style = Paint.Style.FILL
        textSize = 28f
        textAlign = Paint.Align.CENTER
        textSkewX = -0.2f
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
                height
            )
        }

        when (heightMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(
                widthSize,
                heightSize
            )
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth * countCategory,
                height
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        if (stores.isNotEmpty()) {
            drawAxes(width, height, canvas)
            drawNet(width, height, canvas)
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

    private fun drawNet(width: Int, height: Int, canvas: Canvas) {
        val xStep = (width - 2 * marginAxes) / (countDays + 1)
        val yStep = (height - 2 * marginAxes) / (maxAmount/yStepAmount)

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

        for (i in 1..maxAmount/yStepAmount) {
            path.reset()
            path.moveTo(marginAxes, marginAxes + yStep * i)
            path.lineTo(width - marginAxes, marginAxes + yStep * i)
            canvas.drawPath(path, paintAxes)
            canvas.drawTextOnPath("""${(yStepAmount * (maxAmount/yStepAmount + 1 - i))} Р""", path, 570f , -10f, paintText)
        }
    }

    private fun drawCategories(width: Int, height: Int, canvas: Canvas) {
        //TODO draw categories grath
    }


    fun setStores(newStores: List<Store>) {
        require(newStores.maxOf { it.amount } <= MAX_AMOUNT ){ MAX_AMOUNT_EXCEPTION }
        require(newStores.minOf { it.amount } > ZERO ){ MAX_AMOUNT_EXCEPTION }

        stores.clear()
        stores.addAll(newStores)

        countCategory = stores.distinctBy { it.category }.size
        countDays = stores.groupingBy { it.category }.eachCount().maxOf { it.value }
        maxAmount = stores.maxOf { it.amount }
        minAmount = stores.minOf { it.amount }
        val delta = maxAmount - minAmount
        yStepAmount =
            when {
                delta / 10f < 1 -> 1
                delta / 100f < 1 -> 10
                delta / 1000f < 1 -> 100
                delta / 10_000f < 1 -> 1000
                delta / 100_000f < 1 -> 10_000
                else -> 100_000
            }

        for (i in 0 until countCategory) {
            Paint().apply {
                color = resources.getColor(Colors.values()[i].rgb, null)
                strokeWidth = stroke
                style = Paint.Style.STROKE
                pieChartPaints.add(this)
            }
        }

        requestLayout()
        invalidate()
    }

    companion object {
        private const val MAX_AMOUNT = 1_000_000
        private const val MAX_AMOUNT_EXCEPTION = "Кол-во трат в день по одной из категорий превышает допустимый $MAX_AMOUNT"
        private const val ZERO = 0
        private const val DAY = "день"
    }
}