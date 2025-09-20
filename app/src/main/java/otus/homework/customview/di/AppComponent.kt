package otus.homework.customview.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import otus.homework.customview.presentation.MainActivity

@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory{
        fun newAppComponent(@BindsInstance context: Context): AppComponent
    }
}
