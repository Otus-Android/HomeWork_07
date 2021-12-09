package otus.homework.customview.chartview

import android.graphics.Color
import android.graphics.Paint
import kotlin.collections.HashMap

class PieData {

    val pieModels: HashMap<String, PieModel> = hashMapOf()
    var currencyLabel: String = "$"
        private set

    fun setNewCurrencyLabel(newCurrencyLabel: String) {
        currencyLabel = newCurrencyLabel
    }

    private val colors = listOf(
        Color.parseColor("#f06292"),
        Color.parseColor("#ff8a65"),
        Color.parseColor("#9575cd"),
        Color.parseColor("#aab6fe"),
        Color.parseColor("#64b5f6"),
        Color.parseColor("#8bf6ff"),
        Color.parseColor("#4db6ac"),
        Color.parseColor("#81c784"),
        Color.parseColor("#dce775"),
        Color.parseColor("#fff176"),
    )

    var totalAmount = 0.0

    fun add(
        name: String,
        amount: Double,
        color: String? = null
    ) {
        if (pieModels.containsKey(name)) {
            pieModels[name]?.let { pieModel ->
                pieModel.amount += amount
            }
        } else {
            pieModels[name] = PieModel(
                name = name,
                amount = amount,
                paint = createPaint(color)
            )
        }
        totalAmount += amount
    }

    private fun createPaint(color: String?): Paint {
        val newPaint = Paint()
        color?.let { colorStr ->
            newPaint.color = Color.parseColor(colorStr)
        } ?: run {
            newPaint.color = colors[pieModels.size % colors.size]
        }
        newPaint.isAntiAlias = true
        return newPaint
    }
}