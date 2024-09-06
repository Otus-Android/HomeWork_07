package otus.homework.customview.presentation.expensesgraph

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.toBundle
import otus.homework.customview.utils.toMap
import kotlin.math.min

class ExpensesGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px

    private val dayToExpenses = mutableMapOf<Int, Int>().withDefault { 0 }
    private var maxCategoryTotalAmount = 0
    private var axisDrawer: AxisDrawer = AxisDrawer(this)
    private var graphDrawer: GraphDrawer? = null

    fun setMaxDailyExpenseOfAllCategories(maxCategoryTotalAmount: Int) {
        this.maxCategoryTotalAmount = maxCategoryTotalAmount
        initGraphDrawer()
    }

    fun setDaysToExpenses(dayToExpenses: Map<Int, Int>) {
        this.dayToExpenses.clear()
        this.dayToExpenses.putAll(dayToExpenses)
        initGraphDrawer()
        invalidate()
    }

    private fun initGraphDrawer() {
        graphDrawer = GraphDrawer(
            this,
            maxCategoryTotalAmount = maxCategoryTotalAmount,
            daysToExpenses = dayToExpenses,
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(widthSize, heightSize)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> defaultWidthPx.coerceAtMost(size)
            else -> defaultWidthPx
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> defaultHeightPx.coerceAtMost(size)
            else -> defaultHeightPx
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        with(axisDrawer) {
            drawTicksOnXAxis(canvas)
            drawTicksOnYAxis(canvas)
            drawAxis(canvas)
            drawDotsAtTickIntersections(canvas)
        }

        graphDrawer?.let {
            it.drawGraph(canvas)
            it.drawDashedLinesThroughGraphPeaksPoints(canvas)
            it.drawPurchaseDots(canvas)
            it.drawTextAboveDashedLines(canvas)
        }
    }


    private val superStateKey = "superState"
    private val selectedCategoryKey = "dayToExpensesKey"
    private val maxCategoryTotalAmountKey = "maxCategoryTotalAmountKey"

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(superStateKey, superState)
        bundle.putInt(maxCategoryTotalAmountKey, maxCategoryTotalAmount)
        bundle.putBundle(selectedCategoryKey, dayToExpenses.toBundle())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable(superStateKey))

        val dayToExpensesBundle = bundle.getBundle(selectedCategoryKey)
        if (dayToExpensesBundle != null) {
            dayToExpenses.putAll(dayToExpensesBundle.toMap())
        }
        maxCategoryTotalAmount = bundle.getInt(maxCategoryTotalAmountKey)
        initGraphDrawer()
        invalidate()
    }
}
