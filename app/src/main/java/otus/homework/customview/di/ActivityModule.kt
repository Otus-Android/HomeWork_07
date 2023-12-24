package otus.homework.customview.di

import android.content.Context
import dagger.Module
import dagger.Provides
import otus.homework.customview.data.ClientProductsRepositoryImpl
import otus.homework.customview.domain.ClientProductsInteractor
import otus.homework.customview.presentation.MainActivityViewModelFactory

@Module
object ActivityModule {

    @Provides
    fun provideViewModelFactory(@ActivityContext context: Context) = MainActivityViewModelFactory(
        interactor = ClientProductsInteractor(
            ClientProductsRepositoryImpl(context.resources),
        )
    )
}