package otus.homework.customview.domain

import otus.homework.customview.data.ExpensesException
import kotlin.coroutines.cancellation.CancellationException

interface ExpensesRepository {

    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getExpenses(): List<Expense>
}