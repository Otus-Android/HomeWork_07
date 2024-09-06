package otus.homework.customview.presentation.model

data class PieChartAngle(
    val id: Int,
    val category: String,
    val startAngle: Float,
    val endAngle: Float,
    val color: PieChartColor
) {

    val sweepAngle: Float
        get() = endAngle - startAngle
}