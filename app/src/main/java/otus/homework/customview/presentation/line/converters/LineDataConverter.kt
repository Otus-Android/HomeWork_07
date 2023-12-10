package otus.homework.customview.presentation.line.converters

import otus.homework.customview.domain.models.Expense
import otus.homework.customview.presentation.line.chart.LineData

class LineDataConverter(private val converter: LineNodeConverter = LineNodeConverter()) {

    fun convert(source: List<Expense>) =
        LineData(nodes = source.map { converter.convert(it) })
}