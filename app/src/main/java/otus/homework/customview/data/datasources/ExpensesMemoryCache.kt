package otus.homework.customview.data.datasources

import otus.homework.customview.data.models.ExpenseEntity

/**
 * ОЗУ кэш данных по расходам
 */
interface ExpensesMemoryCache : ExpensesDataSource {

    /** Сохранить список записей [ExpenseEntity] */
    fun saveExpenses(expenses: List<ExpenseEntity>)

    /** Очистить сохраненные записи */
    fun clear()
}