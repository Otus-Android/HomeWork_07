package otus.homework.customview.chart

import kotlinx.serialization.Serializable

@Serializable
data class ChartData (
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)