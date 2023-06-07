package otus.homework.customview

import android.app.Application
import java.io.InputStream

class MyApp: Application() {
    companion object{
    lateinit var myResource: InputStream}
    override fun onCreate(){
    super.onCreate()
       myResource =  this.resources.openRawResource(R.raw.payload)
    }

}