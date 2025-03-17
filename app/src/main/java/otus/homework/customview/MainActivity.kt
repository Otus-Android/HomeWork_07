package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.Json
import otus.homework.customview.PieChartView.DataItem
import otus.homework.customview.SimpleStockChartView.ChartItem
import otus.homework.customview.databinding.ActivityMainBinding
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        if (savedInstanceState == null) {
            createChart()
        }
        binding.pieChartView.setOnSectorClickListener(object : PieChartView.OnSectorClickListener {
            override fun onSectorClicked(sector: DataItem?) {
                sector?.let {
                    val products = productList()
                    val item = products.find { it.id == sector.id }
                    val items = products
                        .filter { it.category==item?.category}
                        .map {
                            val instant = Instant.ofEpochSecond(it.time.toLong())
                            ChartItem(
                                instant.atZone(ZoneId.systemDefault()).toLocalDate(),
                                it.amount.toDouble()
                            )
                        }
                    binding.simpleStockChartView.setData(items)
                }
            }
        })
        setContentView(view)
    }

    private fun createChart() {
        val items = productList().map {
            val color =
                Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            DataItem(it.id, it.name, color, it.amount.toFloat())
        }
        binding.pieChartView.setData(items)
    }

    private fun productList(): List<Product> {
        val dataStr = readRawFileAsString(this, R.raw.payload)
        val data = Json.decodeFromString<List<Product>>(dataStr)
        return data
    }
}