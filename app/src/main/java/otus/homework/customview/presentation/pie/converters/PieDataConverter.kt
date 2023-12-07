package otus.homework.customview.presentation.pie.converters

import otus.homework.customview.domain.Expense
import otus.homework.customview.presentation.pie.chart.PieData

class PieDataConverter(private val pieNodeConverter: PieNodeConverter = PieNodeConverter()) {

    fun convert(source: List<Expense>): PieData = PieData(
        nodes = source.map { pieNodeConverter.convert(it) }
    )
}