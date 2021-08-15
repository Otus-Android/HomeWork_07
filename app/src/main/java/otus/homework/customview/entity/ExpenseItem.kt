package otus.homework.customview.entity

data class ExpenseItem(
    val amount: Int,
    val category: String,
    val id: Int,
    val name: String,
    val time: Long
)