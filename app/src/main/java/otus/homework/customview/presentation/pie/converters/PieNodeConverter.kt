package otus.homework.customview.presentation.pie.converters

import android.graphics.Color
import otus.homework.customview.domain.Expense
import otus.homework.customview.presentation.pie.chart.PieNode
import kotlin.random.Random

class PieNodeConverter {

    fun convert(source: Expense) = PieNode(
        value = source.amount.toFloat(),
        label = source.category,
        color = nextColor()
    )

    private fun nextColor() = Color.argb(
        MAX_COLOR,
        Random.nextInt(MAX_COLOR),
        Random.nextInt(MAX_COLOR),
        Random.nextInt(MAX_COLOR)
    )

    private companion object {
        const val MAX_COLOR = 255
    }
}