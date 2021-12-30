package otus.homework.customview.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import otus.homework.customview.JsonDataRepository
import otus.homework.customview.MainActivity
import otus.homework.customview.PieChartViewModelFactory

@ActivityScope
@Component(
    modules = [ActivityModule::class]
)
interface ActivityComponent {

    fun inject(activity: MainActivity)

    fun provideActivityContext(): Context
    fun provideJsonDataRepository(): JsonDataRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ActivityComponent
    }
}

@Module
abstract class ActivityModule {
    @Binds
    abstract fun bindViewModelFactory(factory: PieChartViewModelFactory): ViewModelProvider.Factory

}
