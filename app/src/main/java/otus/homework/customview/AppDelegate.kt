package otus.homework.customview

import android.app.Application
import otus.homework.customview.di.AppComponent
import otus.homework.customview.di.DaggerAppComponent

class AppDelegate : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }
}