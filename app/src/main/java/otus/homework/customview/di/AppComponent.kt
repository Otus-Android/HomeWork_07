package otus.homework.customview.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import otus.homework.customview.MainActivity
import otus.homework.customview.SpendingDetailsActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(mainActivity: MainActivity)
    fun inject(graphActivity: SpendingDetailsActivity)
}