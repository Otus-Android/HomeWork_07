package otus.homework.customview

import android.app.Application
import android.content.Context
import otus.homework.customview.di.DiContainer

/**
 * `Custom application`, предоставляющий контейнер зависимостей
 */
class MyApplication : Application() {

    /** Контейнер зависимостей */
    lateinit var diContainer: DiContainer

    override fun onCreate() {
        super.onCreate()
        diContainer = DiContainer(this)
    }

    companion object {

        /** Получить контейнер зависимостей на основе контекста [Context] */
        fun diContainer(context: Context) =
            (context.applicationContext as MyApplication).diContainer
    }
}