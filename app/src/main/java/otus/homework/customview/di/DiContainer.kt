package otus.homework.customview.di

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import otus.homework.customview.data.ExpensesRepositoryImpl
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.data.datasources.ExpensesMemoryCache
import otus.homework.customview.data.datasources.FileDataSource
import otus.homework.customview.data.datasources.MemoryDataSource
import otus.homework.customview.data.datasources.RandomDataSource
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.domain.ExpensesInteractorImpl
import otus.homework.customview.domain.ExpensesRepository
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesConfigImpl
import otus.homework.customview.domain.config.ExpensesProviderType
import java.util.EnumMap

/**
 * Контейнер зависимостей
 *
 * @param context `application context`
 */
class DiContainer(context: Context) {

    /** Интерактор данных по расходам */
    val interactor: ExpensesInteractor by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesInteractorImpl(repository)
    }

    /** Репозиторий данных по расходам */
    private val repository: ExpensesRepository by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesRepositoryImpl(dataSources, memoryCache, config, ExpensesConverter())
    }

    /** Хранилище источников данных типа "ключ - значение" */
    private val dataSources by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        EnumMap(
            mapOf(
                ExpensesProviderType.FILE to fileDataSource,
                ExpensesProviderType.RANDOM to randomDataSource
            )
        )
    }

    /** Файловый источник данных записей по расходам */
    private val fileDataSource: ExpensesDataSource by lazy(LazyThreadSafetyMode.NONE) {
        FileDataSource(context, ObjectMapper())
    }

    /** Источник данных случайных записей по расходам */
    private val randomDataSource: ExpensesDataSource by lazy(LazyThreadSafetyMode.NONE) {
        RandomDataSource(context.resources)
    }

    /** ОЗУ кэш записей по расходам */
    private val memoryCache: ExpensesMemoryCache by lazy(LazyThreadSafetyMode.NONE) { MemoryDataSource() }

    /** Конфигурационные данные по расходам */
    val config: ExpensesConfig by lazy(LazyThreadSafetyMode.NONE) { ExpensesConfigImpl() }
}