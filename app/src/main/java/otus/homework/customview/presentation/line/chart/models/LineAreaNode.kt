package otus.homework.customview.presentation.line.chart.models

import java.util.Date

data class LineAreaNode(
    val x: Float, // time
    val y: Float, // value
    val label: String?,
    val date: Date,
    //  val payload: T? = null
)