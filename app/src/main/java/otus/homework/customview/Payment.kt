package otus.homework.customview

import com.google.gson.annotations.SerializedName

data class Payment(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("time") val time: Int
)

