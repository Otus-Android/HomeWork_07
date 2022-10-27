package otus.homework.customview.models


import com.google.gson.annotations.SerializedName

data class Metka(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("time")
    val time: Long
)