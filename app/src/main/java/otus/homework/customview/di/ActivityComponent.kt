package otus.homework.customview.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import otus.homework.customview.presentation.MainActivity

@Component(
    modules = [ActivityModule::class]
)
interface ActivityComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @ActivityContext context: Context
        ): ActivityComponent
    }

    /**
     * Иньекция зависимостей в [MainActivity]
     */
    fun inject(activity: MainActivity)
}