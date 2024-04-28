package otus.homework.customview

import com.google.gson.annotations.SerializedName

data class Consumption(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("time") val time: Long,
)