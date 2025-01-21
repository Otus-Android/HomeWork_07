package otus.homework.customview

import android.content.Context
import kotlinx.serialization.json.Json


class ExpensesRepository(private val context: Context) {
    fun getExpenses(): List<ExpensesInfoItem> {
        val payloadString = context.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        return Json.decodeFromString<List<ExpensesInfoItem>>(payloadString)
    }

    fun getExpensesByCategory(category: String): List<ExpensesInfoItem> {
        return getExpenses().filter { it.category == category }
    }
}