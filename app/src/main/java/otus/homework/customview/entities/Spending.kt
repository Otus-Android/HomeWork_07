package otus.homework.customview.entities

import com.google.gson.annotations.SerializedName

data class Spending(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("amount") var amount: Int = 0,
    @SerializedName("category") val category: String = "",
    @SerializedName("time") val time: Int = 0
)