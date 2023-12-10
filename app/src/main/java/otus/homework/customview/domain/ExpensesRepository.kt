package otus.homework.customview.domain

import otus.homework.customview.domain.models.Expense
import otus.homework.customview.domain.models.ExpensesException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Репозиторий данных по расходам
 */
interface ExpensesRepository {

    /**
     * Получить данные по расходам
     *
     * @param max макимально возможное кол-во записей по расходам
     *
     * @throws ExpensesException при ошибке получения записей по расходам
     * @throws CancellationException при отмене получения
     */
    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getExpenses(max: Int? = null, force: Boolean): List<Expense>
}