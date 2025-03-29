package otus.homework.customview.presentation.firstPieChart

import android.content.Context
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.domain.Spend

class PieChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {
    private val spends = mutableListOf<Spend>()

    init {
//        if (isInEditMode) {
//
//        }
    }

    fun setData(newSpends: List<Spend>) {
        println("$newSpends")
        spends.addAll(newSpends)
    }
}