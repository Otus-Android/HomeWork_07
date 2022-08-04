package otus.homework.customview.piechart

import otus.homework.customview.PayloadDto

data class PieChartSection(
    val pieChartItems: List<PayloadDto>,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Int
)