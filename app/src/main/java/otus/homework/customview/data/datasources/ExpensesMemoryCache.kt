package otus.homework.customview.data.datasources

import androidx.annotation.AnyThread
import otus.homework.customview.data.models.ExpenseEntity

/**
 * ОЗУ кэш данных по расходам
 */
interface ExpensesMemoryCache : ExpensesDataSource {

    /** Сохранить список записей [ExpenseEntity] */
    @AnyThread
    fun saveExpenses(expenses: List<ExpenseEntity>)

    /** Очистить сохраненные записи */
    @AnyThread
    fun clear()
}