package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class StatisticsViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics_view)

        val jsonData = applicationContext.resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }

        val uiData = Gson().fromJson(jsonData, SegmentsDataEntity::class.java)

        val statisticView = findViewById<StatisticsView>(R.id.statisticView)

        if (savedInstanceState == null) {
            statisticView.setData(uiData)
        }
    }
}