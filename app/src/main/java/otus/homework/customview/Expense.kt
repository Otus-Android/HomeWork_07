package otus.homework.customview

import android.content.Context
import com.google.gson.Gson

data class Expense(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)

fun Context.loadExpenses(): List<Expense> {
    val input = resources.openRawResource(R.raw.payload)
    val json = input.bufferedReader().use { it.readText() }
    return Gson().fromJson(json, Array<Expense>::class.java).toList()
}

