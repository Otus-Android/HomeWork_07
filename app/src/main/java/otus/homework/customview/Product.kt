package otus.homework.customview

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("amount")
    val amount: Int,
    @SerialName("category")
    val category: String,
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("time")
    val time: Int
)