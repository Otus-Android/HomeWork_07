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
fun getPieChartColors(): Array<Int> {
    return arrayOf(
        Color.BLACK,
        Color.WHITE,
        Color.BLUE,
        Color.GREEN,
        Color.RED,
        Color.YELLOW,
        Color.GRAY,
        Color.DKGRAY,
    )
}