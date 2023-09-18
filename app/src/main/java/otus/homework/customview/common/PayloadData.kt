package otus.homework.customview.common

import kotlinx.serialization.Serializable

@Serializable
data class PayloadData(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)