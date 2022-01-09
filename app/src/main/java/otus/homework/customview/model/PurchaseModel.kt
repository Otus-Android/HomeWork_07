package otus.homework.customview.model

import com.google.gson.annotations.SerializedName


data class PurchaseModel(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("amount")
    val amount: String,
    @field:SerializedName("category")
    val category: String,
    @field:SerializedName("time")
    val time: Int
)
