package otus.homework.customview.presentation.line.converters

import otus.homework.customview.domain.models.Expense
import otus.homework.customview.presentation.line.chart.LineNode

class LineNodeConverter {

    fun convert(source: Expense) = LineNode(
        value = source.amount.toFloat(),
        time = source.time,
        label = source.category,
    )
}