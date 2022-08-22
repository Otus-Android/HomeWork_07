package otus.homework.customview

import com.google.gson.annotations.SerializedName

data class Expense(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("amount")
    val amount: Int,

    @field:SerializedName("category")
    val category: String,

    @field:SerializedName("time")
    val time: Long,
)
