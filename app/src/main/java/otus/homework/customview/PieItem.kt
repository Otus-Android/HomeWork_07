package otus.homework.customview
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PieItem(
    val id: Int,
    val name: String,
    var value: Int,
    var startAngle: Float,
    var angle: Float
) : Serializable

fun Expence.toPieItem():PieItem {
    return PieItem(this.id,this.category,this.amount,0f,0f)
}