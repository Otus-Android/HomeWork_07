package otus.homework.customview

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun getDataItemList(context: Context): List<DataItem>? {
    val jsonString: String? = try {
        context.resources.openRawResource(R.raw.payload).bufferedReader().readText()
    } catch (ex: Exception) {
        null
    }

    return jsonString?.let { Json.decodeFromString<List<DataItem>>(it) }
}

@Serializable
data class DataItem(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)