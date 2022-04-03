package otus.homework.customview

data class SpendingModel(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)