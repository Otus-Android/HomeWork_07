package otus.homework.customview.models

data class SpendItem(
    val id: Long = 0,
    val name: String = "",
    val amount: Int = 0,
    val category: String = "",
    val time: Long = 0
)
