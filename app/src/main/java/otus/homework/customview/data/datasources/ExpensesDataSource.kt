package otus.homework.customview.data.datasources

import androidx.annotation.WorkerThread
import otus.homework.customview.data.models.ExpenseEntity
import otus.homework.customview.data.models.ExpensesDataException

/**
 * Источник данных по расходам
 */
interface ExpensesDataSource {

    /**
     * Получить данные по расходам
     *
     * @param max максимально возможное кол-во записей по расходам (`null` - ограничения отсутствуют)
     *
     * @throws ExpensesDataException при ошибке получения данных
     */
    @Throws(ExpensesDataException::class)
    @WorkerThread
    fun getExpenses(max: Int? = null): List<ExpenseEntity>
}