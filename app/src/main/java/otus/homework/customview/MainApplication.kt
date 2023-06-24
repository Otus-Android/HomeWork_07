package otus.homework.customview

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this);
    }
}