package otus.homework.customview.presentation.line.chart

/**
 * Данные линейного графика
 *
 * @param name наименование графика
 * @param nodes узлы линейного графика
 */
data class LineData(
    val name: String? = null,
    val nodes: List<LineNode> = emptyList()
)
