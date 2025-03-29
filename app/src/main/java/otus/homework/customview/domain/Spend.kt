package otus.homework.customview.domain

import kotlinx.serialization.Serializable

@Serializable
data class Spend(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: Category,
    val time: Long,
)
