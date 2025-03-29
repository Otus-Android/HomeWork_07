package otus.homework.customview

import android.app.Application
import otus.homework.customview.data.Repository

class CustomViewApp: Application() {
    val repository by lazy { Repository(applicationContext) }
}