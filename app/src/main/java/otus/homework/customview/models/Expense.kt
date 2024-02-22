package otus.homework.customview.models

import kotlinx.serialization.Serializable

typealias Category = List<Expense>

@Serializable
data class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)
