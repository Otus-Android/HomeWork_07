package otus.homework.customview.data.datasources

import otus.homework.customview.data.models.ExpenseEntity


class MemoryDataSource : ExpensesMemoryCache {

    private val cache = mutableListOf<ExpenseEntity>()

    override fun saveExpenses(expenses: List<ExpenseEntity>) {
        cache.clear()
        cache.addAll(expenses)
    }

    override fun getExpenses(max: Int?): List<ExpenseEntity> = cache

    override fun clear() = cache.clear()
}