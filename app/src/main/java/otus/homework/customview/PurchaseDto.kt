package otus.homework.customview

// pojo from json
data class PurchaseDto(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)