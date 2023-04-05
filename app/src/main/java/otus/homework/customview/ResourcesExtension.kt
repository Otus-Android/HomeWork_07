package otus.homework.customview

import android.content.Context
import android.graphics.Color
import org.json.JSONArray


private const val ID = "id"
private const val NAME = "name"
private const val AMOUNT = "amount"
private const val CATEGORY = "category"
private const val TIME = "time"

fun Context.getExpensesFromRawFile(): List<Outlay> {
    val jsonArray = JSONArray(resources.openRawResource(R.raw.payload).reader().readText())
    return ( 0 until jsonArray.length()).map {
        val jsonObj = jsonArray.getJSONObject(it)
        return@map Outlay(
            jsonObj.optInt(ID),
            jsonObj.optString(NAME, ""),
            jsonObj.optInt(AMOUNT),
            jsonObj.optString(CATEGORY, ""),
            jsonObj.optLong(TIME)
        )
    }
}
fun Context.getPieChartColors(): Array<Int> {
    return arrayOf(
        getColor(R.color.purple_200),
        getColor(R.color.purple_500),
        getColor(R.color.purple_700),
        getColor(R.color.teal_200),
        getColor(R.color.teal_700),
        getColor(R.color.black),
        getColor(R.color.white),
        getColor(R.color.orange),
        getColor(R.color.red),
        getColor(R.color.green),
        getColor(R.color.purple_200),
    )
}
fun Int.dp(context: Context) = context.resources.displayMetrics.density * this
fun Float.dp(context: Context) = context.resources.displayMetrics.density * this