package otus.homework.customview

import android.content.Context

class ExpensesRepository(
    private val context: Context
) {
    fun getExpenses() = context.getExpensesFromRawFile()
}