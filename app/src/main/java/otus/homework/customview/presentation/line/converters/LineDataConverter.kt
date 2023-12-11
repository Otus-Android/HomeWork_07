package otus.homework.customview.presentation.line.converters

import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.line.chart.LineData

/**
 * Конвертер данных линейного графика
 *
 * @param converter конвертер данных узла линейного графика
 */
class LineDataConverter(private val converter: LineNodeConverter = LineNodeConverter()) {

    /** Конвертировать категорию [Category] в данные линейного графика */
    fun convert(source: Category) = LineData(name = source.name, nodes = converter.convert(source))

    /** Конвертировать список категорий [Category] в список данных линейного графика */
    fun convert(source: List<Category>) = source.map { convert(it) }
}