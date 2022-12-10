package otus.homework.customview.pie_chart

import otus.homework.customview.dto.PayloadDto

data class PieChartItem(
    val pieChartItems: List<PayloadDto>,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Int
)