package otus.homework.customview.data.datasources

import otus.homework.customview.data.models.ExpenseEntity

/**
 * Источник данных по расходам из оперативной памяти
 */
class MemoryDataSource : ExpensesMemoryCache {

    private val cache = mutableListOf<ExpenseEntity>()

    @Synchronized
    override fun saveExpenses(expenses: List<ExpenseEntity>) {
        cache.clear()
        cache.addAll(expenses)
    }

    @Synchronized
    override fun getExpenses(max: Int?): List<ExpenseEntity> = cache.toList()

    @Synchronized
    override fun clear() = cache.clear()
}