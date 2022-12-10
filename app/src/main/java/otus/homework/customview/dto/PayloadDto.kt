package otus.homework.customview.dto

data class PayloadDto(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)