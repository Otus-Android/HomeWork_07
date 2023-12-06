package otus.homework.customview.di

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import otus.homework.customview.data.ExpensesRepositoryImpl
import otus.homework.customview.data.converters.ExpensesConverter
import otus.homework.customview.data.datasources.ExpensesDataSource
import otus.homework.customview.data.datasources.ExpensesDataSourceImpl
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.domain.ExpensesInteractorImpl
import otus.homework.customview.domain.ExpensesRepository

class DiContainer(context: Context) {

    val interactor: ExpensesInteractor by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesInteractorImpl(repository)
    }

    private val repository: ExpensesRepository by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesRepositoryImpl(dataSource, ExpensesConverter())
    }

    private val dataSource: ExpensesDataSource by lazy(LazyThreadSafetyMode.NONE) {
        ExpensesDataSourceImpl(context, ObjectMapper())
    }
}