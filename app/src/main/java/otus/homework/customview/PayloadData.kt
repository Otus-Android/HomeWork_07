package otus.homework.customview


data class PayloadData(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)