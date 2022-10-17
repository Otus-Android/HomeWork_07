package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.pieChart.ChartPart
import otus.homework.customview.pieChart.PieChartView
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chartView: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartView = findViewById(R.id.chart)

        val chartParts = createChartParts()
        chartView.drawChartParts(chartParts)
    }

    private fun createChartParts(): List<ChartPart> {

        val models = readJsonFile()
        // считаем общую сумму. Для точности переводим в Float
        val totalAmount = models.sumOf { it.amount }.toFloat()

        val chartParts = mutableListOf<ChartPart>()
        models.groupBy { it.category }.forEach { map ->
            val amount = map.value.sumOf { it.amount }.toFloat()
            chartParts.add(ChartPart(map.key, amount, totalAmount, generateColor()))
        }

        return chartParts
    }

    private fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun readJsonFile(): List<JsonModel> {
        val identifier = resources.getIdentifier("payload", "raw", packageName)
        val inputStream = resources.openRawResource(identifier)
        val reader: Reader = InputStreamReader(inputStream)
        val type: Type = object : TypeToken<List<JsonModel>>() {}.type
        return Gson().fromJson(reader, type)
    }
}