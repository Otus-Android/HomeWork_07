package otus.homework.customview.domain.models

data class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)