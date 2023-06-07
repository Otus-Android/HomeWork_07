package otus.homework.customview

import kotlinx.serialization.Serializable

@Serializable
data class Charge(
    val name: String,
    val category: String,
    val amount: Int,
    val time: Long,
    val id: Int,
)
