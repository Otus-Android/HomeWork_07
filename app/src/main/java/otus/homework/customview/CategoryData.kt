package otus.homework.customview

data class CategoryData(
    val category: String,
    val amount: Double,
    val color: Int,
    val transactions: List<Transaction> = emptyList()
)
