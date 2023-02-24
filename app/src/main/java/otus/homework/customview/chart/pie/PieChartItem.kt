package otus.homework.customview.chart.pie

import otus.homework.customview.PayloadItem

data class PieChartItem(
    val startAngle: Float,
    val valueAngle: Float,
    val pieChartItems: List<PayloadItem>,
    val color: Int
)