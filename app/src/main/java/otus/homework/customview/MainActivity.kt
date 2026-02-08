package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val jsonString = resources.openRawResource(R.raw.payload)
            .bufferedReader().use { it.readText() }
        findViewById<PieChartView>(R.id.pieChart)
            .setData(Gson().fromJson(jsonString,
                object : TypeToken<List<Transaction>>() {}.type))
    }
}