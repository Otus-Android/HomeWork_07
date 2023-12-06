package otus.homework.customview.presentation.pie.chart.models

data class PieData<out T : Number>(
    val nodes: List<PieNode<T>>
)
