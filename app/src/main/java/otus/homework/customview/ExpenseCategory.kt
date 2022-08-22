package otus.homework.customview

data class ExpenseCategory(
    val title: String,
    val totalAmount: Int,

    val dates: List<Long>,
    val amounts: List<Int>,

    val color: Int
)
