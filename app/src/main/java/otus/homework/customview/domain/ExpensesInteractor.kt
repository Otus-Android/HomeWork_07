package otus.homework.customview.domain

import kotlinx.coroutines.CancellationException
import otus.homework.customview.data.ExpensesException

interface ExpensesInteractor {

    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getExpenses(max: Int? = null, force: Boolean = true): List<Expense>

    @Throws(ExpensesException::class, CancellationException::class)
    suspend fun getCategories(maxExpenses: Int? = null): List<Category>
}