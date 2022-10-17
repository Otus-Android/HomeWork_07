package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.pieChart.PieChartSector
import otus.homework.customview.pieChart.PieChartView
import java.lang.reflect.Type
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chartView: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartView = findViewById(R.id.chart)

        val chartParts = createChartParts()
        if (savedInstanceState == null) {
            chartView.drawChartParts(chartParts)
        }
    }

    private fun createChartParts(): List<PieChartSector> {

        val models = readJsonFile()
        // считаем общую сумму. Для точности переводим в Float
        val totalAmount = models.sumOf { it.amount }.toFloat()

        val pieChartSectors = mutableListOf<PieChartSector>()
        models.groupBy { it.category }.forEach { map ->
            val amount = map.value.sumOf { it.amount }.toFloat()
            pieChartSectors.add(PieChartSector(map.key, amount, totalAmount, generateColor()))
        }

        return pieChartSectors
    }

    private fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun readJsonFile(): List<JsonModel> {
        val jsonData = applicationContext.resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }

        val type: Type = object : TypeToken<List<JsonModel>>() {}.type
        return Gson().fromJson(jsonData, type)
    }
}