package otus.homework.customview


data class Expenses(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    var time: Long
)
