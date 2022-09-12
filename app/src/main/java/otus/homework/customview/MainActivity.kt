package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val gson = Gson()
//        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
//        val dataPayLoad: Array<PayLoad> = gson.fromJson(buffer, Array<PayLoad>::class.java)

        setContentView(R.layout.activity_main)
    }
}