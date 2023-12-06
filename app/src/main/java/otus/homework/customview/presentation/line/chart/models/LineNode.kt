package otus.homework.customview.presentation.line.chart.models

import java.util.Date

data class LineNode(
    val x: Float, // day
    val y: Float, // amount
    val label: String,
    val color: Int,
    val date: Date
)