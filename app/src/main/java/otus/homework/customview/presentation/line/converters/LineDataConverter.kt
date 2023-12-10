package otus.homework.customview.presentation.line.converters

import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.line.chart.LineData

class LineDataConverter(private val converter: LineNodeConverter = LineNodeConverter()) {

    fun convert(source: Category) =
        LineData(name = source.name, nodes = converter.convert(source))

    fun convert(source: List<Category>) = source.map { convert(it) }
}