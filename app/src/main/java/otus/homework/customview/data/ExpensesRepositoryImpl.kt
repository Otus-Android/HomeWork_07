package otus.homework.customview.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.domain.ExpensesRepository

class ExpensesRepositoryImpl(
    private val dataSource: ExpensesDataSource,
    private val converter: ExpensesConverter
) : ExpensesRepository {

    override suspend fun getExpenses() = withContext(Dispatchers.IO) {
        try {
            val expenses = runInterruptible { dataSource.getExpenses() }
            expenses.map { converter.convert(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ExpensesException(e)
        }
    }
}