package otus.homework.customview

import android.app.Application
import otus.homework.customview.di.AppComponent
import otus.homework.customview.di.DaggerAppComponent

class App: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().newAppComponent(baseContext)
    }
}

fun Application.getComponent() = (this as App).appComponent