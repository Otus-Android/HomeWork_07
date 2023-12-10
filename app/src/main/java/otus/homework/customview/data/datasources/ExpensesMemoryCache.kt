package otus.homework.customview.data.datasources

import otus.homework.customview.data.ExpenseEntity

interface ExpensesMemoryCache : ExpensesDataSource {

    fun saveExpenses(expenses: List<ExpenseEntity>)

    fun clear()
}