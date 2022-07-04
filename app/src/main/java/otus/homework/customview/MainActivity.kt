package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonData = applicationContext.resources.openRawResource(R.raw.payload)
        .bufferedReader().use { it.readText() }

        val uiData = Gson().fromJson(jsonData, SegmentsDataEntity::class.java)

        val myCustomView = findViewById<MyCustomView>(R.id.myView)
        myCustomView.setData(uiData)
    }
}