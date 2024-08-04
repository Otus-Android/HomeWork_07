package otus.homework.customview.pieChart

import android.content.Context
import org.json.JSONArray
import otus.homework.customview.R

data class PieChartData(
    val amount: Int,
    val category: String
)

fun parseData(
    context: Context
) : List<PieChartData> {
    val json = JSONArray(
        context.resources
            .openRawResource(R.raw.payload)
            .reader()
            .readText()
    )

    val dataList = mutableListOf<PieChartData>()
    for (i in 0 until json.length()) {
        val item = json.getJSONObject(i)
        dataList.add(
            PieChartData(
                amount = item.getInt("amount"),
                category = item.getString("category")
            )
        )
    }
    val aggregatedData = dataList.groupBy { it.category }
        .map { (category, items) ->
            PieChartData(
                amount = items.sumOf { it.amount },
                category = category
            )
        }
    return aggregatedData
}