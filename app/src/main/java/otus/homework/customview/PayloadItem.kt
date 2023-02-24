package otus.homework.customview

data class PayloadItem(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
    val date: String = "",
    val numberDay: Int = 0
)