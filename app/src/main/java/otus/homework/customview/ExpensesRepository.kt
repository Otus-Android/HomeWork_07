package otus.homework.customview


import android.content.Context
import org.json.JSONArray

class ExpensesRepository(private val context: Context) {

    fun getExpenses(): List<Expense> {
        val jsonArray = JSONArray(context.resources.openRawResource(R.raw.payload).reader().readText())
        return ( 0 until jsonArray.length()).map {
            val jsonObj = jsonArray.getJSONObject(it)
            return@map Expense(
                jsonObj.optInt("id"),
                jsonObj.optString("name", ""),
                jsonObj.optInt("amount"),
                jsonObj.optString("category", ""),
                jsonObj.optLong("time")
            )
        }
    }
}
