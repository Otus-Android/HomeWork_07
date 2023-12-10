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
import otus.homework.customview.domain.config.ExpensesProvider
import java.util.EnumMap

class DiContainer(context: Context) {

    val interactor: ExpensesInteractor by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesInteractorImpl(repository)
    }

    private val repository: ExpensesRepository by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesRepositoryImpl(dataSources, memoryCache, config, ExpensesConverter())
    }

    private val dataSources by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        EnumMap(
            mapOf(
                ExpensesProvider.ORIGIN to localDataSource,
                ExpensesProvider.RANDOM to randomDataSource
            )
        )
    }

    private val localDataSource: ExpensesDataSource by lazy(LazyThreadSafetyMode.NONE) {
        FileDataSource(context, ObjectMapper())
    }

    private val randomDataSource: ExpensesDataSource by lazy(LazyThreadSafetyMode.NONE) {
        RandomDataSource(context.resources)
    }

    private val memoryCache: ExpensesMemoryCache by lazy(LazyThreadSafetyMode.NONE) {
        MemoryDataSource()
    }

    val config: ExpensesConfig by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesConfigImpl()
    }
}