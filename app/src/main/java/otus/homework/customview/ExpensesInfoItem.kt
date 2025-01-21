package otus.homework.customview

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpensesInfoItem(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("amount")
    val amount: Int = 0,
    @SerialName("category")
    val category: String = "",
    @SerialName("time")
    val time: Long = 0
)
