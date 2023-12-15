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
     * @param force признак форсированной загрузки
     * При `true` осуществляется обновление данных, при `false` - получение данных из кэша.
     * При отсутствии данных в кэше производится обновление.
     *
     * @throws ExpensesException при ошибке получения записей по расходам
     * @throws CancellationException при отмене получения
     */
    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getExpenses(max: Int? = null, force: Boolean): List<Expense>
}