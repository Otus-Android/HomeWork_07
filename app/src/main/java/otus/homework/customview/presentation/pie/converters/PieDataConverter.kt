package otus.homework.customview.presentation.pie.converters

import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.pie.chart.PieData

class PieDataConverter(private val pieNodeConverter: PieNodeConverter = PieNodeConverter()) {

    fun convert(source: List<Category>): PieData = PieData(
        nodes = source.map { pieNodeConverter.convert(it) }
    )
}