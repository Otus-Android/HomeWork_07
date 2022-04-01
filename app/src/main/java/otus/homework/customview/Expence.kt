package otus.homework.customview

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Expence(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("time") val time: Int,
    var startAngle: Float,
    var angle: Float
) : Serializable