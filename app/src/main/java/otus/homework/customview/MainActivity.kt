package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.pieChart.ChartItemModel
import otus.homework.customview.pieChart.PieChart
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val modelItems = readJsonFile()
        val chart: PieChart = findViewById(R.id.chart)
        chart.setModels(modelItems)
    }

    private fun readJsonFile(): List<ChartItemModel> {
        val identifier = resources.getIdentifier("payload", "raw", packageName)
        val inputStream = resources.openRawResource(identifier)
        val reader: Reader = InputStreamReader(inputStream)
        val type: Type = object : TypeToken<List<ChartItemModel>>() {}.type
        return Gson().fromJson(reader, type)
    }
}