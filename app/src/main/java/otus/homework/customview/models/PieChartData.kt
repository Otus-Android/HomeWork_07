package otus.homework.customview.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PieChartData(
    val items: List<Item>
){
    @Serializable
    data class Item(
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("amount")
    val amount: Int,
    @field:SerializedName("category")
    val category: String,
    @field:SerializedName("time")
    val time: Long,
    )
}

