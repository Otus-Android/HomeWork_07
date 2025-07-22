package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import java.util.Random

const val DEFAULT_ANGLE = 0.0
const val MAX_SWEEP_ANGLE = 360

data class PayloadData(
    val id: Int,
    val time: Long,
    val name: String,
    val amount: Double,
    val category: String,
)

@Serializable
data class PieData(
    val color: Int,
    val name: String,
    val startAngle: Double,
    val sweepAngle: Double
)

@Serializable
data class Category(
    val color: Int,
    val value: Double,
    val name: String
)

@Serializable
data class SavedPiecesState(
    val pieces: List<PieData>
)

@Serializable
data class SavedCategoryState(
    val charts: List<Category>
)

fun List<PayloadData>.mapPieces(): List<PieData> {
    val totalSum = this.sumOf { it.amount }
    var startAngle = DEFAULT_ANGLE

    return this.map { payload ->
        val sweepAngle = payload.amount.calculateSweepAngle(totalSum)
        val data = PieData(
            name = payload.name,
            color = generateColor(),
            startAngle = startAngle,
            sweepAngle = sweepAngle
        )
        startAngle += sweepAngle
        data
    }
}

fun List<PayloadData>.mapCategories(): List<Category> = this.map {
    Category(
        name = it.name,
        value = it.amount,
        color = generateColor()
    )
}

private fun generateColor(): Int {
    val rnd = Random()
    return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
}

private fun Double.calculateSweepAngle(total: Double): Double = this / total * MAX_SWEEP_ANGLE

fun Context.readData(): List<PayloadData> {
    val data = this.resources.openRawResource(R.raw.payload)
    val str = data.bufferedReader().use { it.readText() }
    val gson = Gson()

    val listType = object : TypeToken<List<PayloadData>>() {}.type
    val itemList: List<PayloadData> = gson.fromJson(str, listType)
    return itemList
}

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()