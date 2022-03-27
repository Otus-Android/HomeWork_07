package otus.homework.customview
import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("time") val time: Int,
    //TODO было бы правильно выделить отдельный тип и преобразовывать в него данные
    var startAngle: Float,
    var angle: Float
) : Serializable
