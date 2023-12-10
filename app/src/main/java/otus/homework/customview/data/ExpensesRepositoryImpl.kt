package otus.homework.customview.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.data.datasources.ExpensesMemoryCache
import otus.homework.customview.data.models.ExpensesException
import otus.homework.customview.domain.ExpensesRepository
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesProvider
import java.util.EnumMap

class ExpensesRepositoryImpl(
    private val dataSources: EnumMap<ExpensesProvider, ExpensesDataSource>,
    private val memoryCache: ExpensesMemoryCache,
    private val config: ExpensesConfig,
    private val converter: ExpensesConverter
) : ExpensesRepository {

    override suspend fun getExpenses(max: Int?, force: Boolean) =
        withContext(Dispatchers.IO) {
            try {
                val expenses = if (force) {
                    delay(STUB_DELAY)
                    val dataSource = dataSources.getValue(config.provider)
                    runInterruptible { dataSource.getExpenses(max) }
                        .also { memoryCache.saveExpenses(it) }
                } else {
                    memoryCache.getExpenses(max)
                }
                expenses.map { converter.convert(it) }
            } catch (e: CancellationException) {
                memoryCache.clear()
                throw e
            } catch (e: Exception) {
                memoryCache.clear()
                throw ExpensesException(e)
            }
        }

    private companion object {
        const val STUB_DELAY = 2000L
    }
}