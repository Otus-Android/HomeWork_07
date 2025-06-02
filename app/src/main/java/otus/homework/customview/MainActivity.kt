package otus.homework.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.view.PieChartView
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = loadExpenses()
        initPieChartView(data)
        initLineChartView(data)
    }

    private fun loadExpenses(): List<PriceData> {
        val json = resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        val type = object : TypeToken<List<PriceData>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun initLineChartView(data: List<PriceData>) {
        binding.lineChart.setData(data, getColorsForGradient())
    }

    private fun initPieChartView(data: List<PriceData>) {
        binding.pieChart.setData(data, getColorsForGradient())
        binding.pieChart.applyCallback(object : PieChartView.Callback {
            override fun onSectorClick(priceData: PriceData) {
                Toast.makeText(
                    this@MainActivity,
                    "${priceData.name} - ${priceData.amount} USD",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getColorsForGradient() = listOf(
        Color.RED to Color.rgb(255, 165, 0),
        Color.BLUE to Color.CYAN,
        Color.rgb(138, 43, 226) to Color.rgb(255, 105, 180),
        Color.GREEN to Color.rgb(50, 205, 50),
        Color.YELLOW to Color.rgb(255, 215, 0),
        Color.rgb(255, 0, 0) to Color.rgb(255, 182, 193),
        Color.rgb(64, 224, 208) to Color.rgb(0, 191, 255),
        Color.BLACK to Color.rgb(169, 169, 169),
        Color.rgb(0, 0, 139) to Color.rgb(135, 206, 250),
        Color.rgb(128, 0, 128) to Color.rgb(230, 230, 250)
    ).shuffled()
}