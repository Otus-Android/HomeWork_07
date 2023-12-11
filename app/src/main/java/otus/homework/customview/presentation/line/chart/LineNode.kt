package otus.homework.customview.presentation.line.chart

/**
 * Данные узла линейного диаграмы
 *
 * @param value значение
 * @param time время (unix метка, ms)
 * @param label подпись
 */
data class LineNode(
    val value: Float,
    val time: Long,
    val label: String?
)
