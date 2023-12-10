package otus.homework.customview.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.data.datasources.ExpensesMemoryCache
import otus.homework.customview.data.models.ExpensesDataException
import otus.homework.customview.domain.ExpensesRepository
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesProviderType
import otus.homework.customview.domain.models.ExpensesException
import java.util.EnumMap

/**
 * Реализация репозитория данных по расходам
 *
 * @param dataSources хранилище источников данных типа "ключ - значение"
 * @param memoryCache ОЗУ кэш данных по расходам
 * @param config конфигурационные данные по расходам
 * @param converter конвертер данных по расходам
 */
class ExpensesRepositoryImpl(
    private val dataSources: EnumMap<ExpensesProviderType, ExpensesDataSource>,
    private val memoryCache: ExpensesMemoryCache,
    private val config: ExpensesConfig,
    private val converter: ExpensesConverter
) : ExpensesRepository {

    override suspend fun getExpenses(max: Int?, force: Boolean) =
        withContext(Dispatchers.IO) {
            try {
                val expenses = if (force) {
                    delay(STUB_DELAY)
                    val dataSource = dataSources.getValue(config.providerType)
                    runInterruptible { dataSource.getExpenses(max) }
                        .also { memoryCache.saveExpenses(it) }
                } else {
                    memoryCache.getExpenses(max)
                }
                expenses.map { converter.convert(it) }
            } catch (e: ExpensesDataException) {
                memoryCache.clear()
                throw ExpensesException(e)
            }
        }

    private companion object {

        /** Время задержкки для имитации длительной работы */
        const val STUB_DELAY = 1000L
    }
}