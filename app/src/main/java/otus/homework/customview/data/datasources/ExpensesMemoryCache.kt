package otus.homework.customview.data.datasources

import otus.homework.customview.data.models.ExpenseEntity

interface ExpensesMemoryCache : ExpensesDataSource {

    fun saveExpenses(expenses: List<ExpenseEntity>)

    fun clear()
}