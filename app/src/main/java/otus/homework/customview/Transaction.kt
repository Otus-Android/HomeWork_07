package otus.homework.customview

data class Transaction(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val time: Long
)
