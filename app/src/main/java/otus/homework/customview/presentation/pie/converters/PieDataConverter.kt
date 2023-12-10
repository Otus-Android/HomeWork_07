package otus.homework.customview.presentation.pie.converters

import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.pie.chart.PieData

/**
 * Конвертер данных кругового графика
 *
 * @param converter конвертер данных узла кругового графика
 */
class PieDataConverter(private val converter: PieNodeConverter = PieNodeConverter()) {

    /** Конвертировать список категорий [Category] в данные кругового графика */
    fun convert(source: List<Category>): PieData = PieData(
        nodes = source.map { converter.convert(it) }
    )
}