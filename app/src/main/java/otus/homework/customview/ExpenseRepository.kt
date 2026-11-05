package otus.homework.customview

import android.content.Context
import org.json.JSONArray

class ExpenseRepository(private val context: Context) {
    fun loadExpenses(): List<Expense> {

        val json = context
            .resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use {
                it.readText()
            }

        val jsonArray = JSONArray(json)

        return buildList {

            for (i in 0 until jsonArray.length()) {

                val item = jsonArray.getJSONObject(i)

                add(
                    Expense(
                        id = item.getInt("id"),
                        name = item.getString("name"),
                        amount = item.getDouble("amount"),
                        category = item.getString("category"),
                        time = item.getLong("time")
                    )
                )
            }
        }
    }
}