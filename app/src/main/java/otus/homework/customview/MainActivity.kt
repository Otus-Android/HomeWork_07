package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inputStream = resources.openRawResource(R.raw.payload)
        val payload = InputStreamReader(inputStream)
        val values = readCosts(payload)

        initPieChartView(values)
        initLineChartView(values)
    }

    private fun initLineChartView(data: List<CostDto>) {
        binding.lineChart.setData(data, getColorsForGradient())
    }

    private fun initPieChartView(data: List<CostDto>) {
        binding.pieChart.setData(data, getColorsForGradient())
        binding.pieChart.applyCallback(object : PieChartView.Callback {
            override fun onSectorClick(costDto: CostDto) {
                Toast.makeText(
                    this@MainActivity,
                    "${costDto.name} - ${costDto.amount} USD",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun readCosts(payload: InputStreamReader): List<CostDto> {
        val costListType = object : TypeToken<List<CostDto>>() {}.type
        return Gson().fromJson(payload, costListType)
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