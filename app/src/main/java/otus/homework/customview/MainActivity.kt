package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.pieChart.ChartPart
import otus.homework.customview.pieChart.PieChart
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chart = findViewById(R.id.chart)


        val chartParts = readJsonFile()

        setUpChartParts(chartParts)

        chart.drawChartParts(chartParts)
    }

    private fun setUpChartParts(chartParts: List<ChartPart>) {
        // считаем общую сумму. Для точности переводим в Float
        val totalAmount = chartParts.sumOf { it.amount }.toFloat()
        // считаем какая сумма соответствует одному проценту
        val oneAmountDegree = totalAmount / 360

        // начальное значение с которого будет строиться график
        var startChartDegreePoint = 0f
        chartParts.forEach {
            // считаем сколько градусов занимает значение
            val partDegreeAngle = it.amount / oneAmountDegree

            it.startAngle = startChartDegreePoint
            it.sweepAngle = partDegreeAngle
            it.color = generateColor()

            startChartDegreePoint += partDegreeAngle
        }
    }

    private fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun readJsonFile(): List<ChartPart> {
        val identifier = resources.getIdentifier("payload", "raw", packageName)
        val inputStream = resources.openRawResource(identifier)
        val reader: Reader = InputStreamReader(inputStream)
        val type: Type = object : TypeToken<List<ChartPart>>() {}.type
        return Gson().fromJson(reader, type)
    }
}