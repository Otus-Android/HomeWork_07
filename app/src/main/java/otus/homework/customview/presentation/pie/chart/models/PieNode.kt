package otus.homework.customview.presentation.pie.chart.models

data class PieNode<out T : Number>(
    val value: T,
    val label: String? = null
)
