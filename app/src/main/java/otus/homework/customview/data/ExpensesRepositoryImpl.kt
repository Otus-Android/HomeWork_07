package otus.homework.customview.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.domain.Expense
import otus.homework.customview.domain.ExpensesRepository
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesProvider
import java.util.EnumMap

class ExpensesRepositoryImpl(
    private val dataSources: EnumMap<ExpensesProvider, ExpensesDataSource>,
    private val config: ExpensesConfig,
    private val converter: ExpensesConverter
) : ExpensesRepository {

    override suspend fun getExpenses(max: Int?): List<Expense> = withContext(Dispatchers.IO) {
        try {
            val dataSource = dataSources.getValue(config.provider)
            val expenses = runInterruptible { dataSource.getExpenses(max) }
            expenses.map { converter.convert(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ExpensesException(e)
        }
    }
}