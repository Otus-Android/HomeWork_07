package otus.homework.customview.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionJson(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)