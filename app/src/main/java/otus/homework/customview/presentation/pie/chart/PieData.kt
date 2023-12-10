package otus.homework.customview.presentation.pie.chart

/**
 * Данные кругового графика
 *
 * @param nodes узлы кругового графика
 */
data class PieData(
    val nodes: List<PieNode> = emptyList()
)
