package otus.homework.customview.di

import android.content.Context
import dagger.Module
import dagger.Provides
import otus.homework.customview.ResourceWrapperImpl
import otus.homework.customview.SpendingRepository
import otus.homework.customview.SpendingRepositoryImpl
import javax.inject.Singleton

@Module
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(context: Context): SpendingRepository {
        val resourceWrapper = ResourceWrapperImpl(context)
        return SpendingRepositoryImpl(resourceWrapper)
    }
}