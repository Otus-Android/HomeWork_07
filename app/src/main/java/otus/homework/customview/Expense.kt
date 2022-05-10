package otus.homework.customview

import com.google.gson.annotations.SerializedName
import java.util.*

data class Expense(
    @SerializedName("amount") val amount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("time") val time: Long
) {
    val date: Date
        get() = Date(time * 1000L).removeTime()
}