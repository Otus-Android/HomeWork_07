package otus.homework.customview.data

import android.content.Context
import kotlinx.serialization.json.Json
import otus.homework.customview.models.PieChartData
import javax.inject.Inject

class UiDataJsonParser @Inject constructor(private val context: Context, private val json: Json) {

    fun parseFromAssets(fileName: String): PieChartData {

        val jsonString = context
            .assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString<PieChartData>(jsonString)
    }
}