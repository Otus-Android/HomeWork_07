package otus.homework.customview

data class ExpensesByDate(
    val category: String,
    val sum: Int,
    var time: Long
)