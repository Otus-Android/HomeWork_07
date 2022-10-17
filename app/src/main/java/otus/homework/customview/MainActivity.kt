package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.linerChart.LinearChartView
import otus.homework.customview.pieChart.PieChartSector
import otus.homework.customview.pieChart.PieChartView
import java.lang.reflect.Type
import java.util.*

const val RADIO_GROUP_KEY = "radio_group_key"

class MainActivity : AppCompatActivity() {

    lateinit var radioButtonGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView: PieChartView = findViewById(R.id.pieChart)
        val linearChartView: LinearChartView = findViewById(R.id.lineChart)

        radioButtonGroup = findViewById(R.id.group)
        radioButtonGroup.setOnCheckedChangeListener { _, id ->
            pieChartView.isVisible = id == R.id.btnPieChart
            linearChartView.isVisible = id == R.id.btnLinearChart
        }

        val chartParts = createChartParts()
        if (savedInstanceState == null) {
            pieChartView.drawChartParts(chartParts)
            radioButtonGroup.check(R.id.btnPieChart)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RADIO_GROUP_KEY, radioButtonGroup.checkedRadioButtonId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        radioButtonGroup.check(savedInstanceState.getInt(RADIO_GROUP_KEY))
    }
}