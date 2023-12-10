package otus.homework.customview.domain

import otus.homework.customview.data.models.ExpensesException
import otus.homework.customview.domain.models.Expense
import kotlin.coroutines.cancellation.CancellationException

interface ExpensesRepository {

    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getExpenses(max: Int? = null, force: Boolean): List<Expense>
}