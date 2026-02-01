package otus.homework.customview

data class Expense(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val time: Long
)

data class CategoryData(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val color: Int
)