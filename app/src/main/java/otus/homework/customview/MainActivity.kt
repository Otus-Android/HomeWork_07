package otus.homework.customview

import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val marketData = parseData()
        val pointList = mapPurchasesToPoints(marketData)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val graph = findViewById<GraphView>(R.id.graph)

        pieChart.setOnSliceClickListener(object : PieChart.OnSliceClickListener {
            override fun onClick(category: String) {
                graph.setData(pointList[category]!!)
            }
        })
        val colors = resources.getStringArray(R.array.chart_colors)
        colors.shuffle()
        val pieData = PieData(colors)
        marketData.map { pieData.add(it.category, it.amount) }
        pieChart.setData(pieData)
    }

    private fun parseData(): List<MarketData> {
        val rowData = JSONArray(
            this.resources
                .openRawResource(R.raw.payload)
                .reader()
                .readText()
        )
        return (0 until rowData.length()).map {
            val obj = rowData.getJSONObject(it)
            return@map MarketData(
                obj.optInt("id"),
                obj.optString("name", ""),
                obj.optInt("amount"),
                obj.optString("category", ""),
                obj.optLong("time")
            )
        }
    }

    private fun mapPurchasesToPoints(payments: List<MarketData>?) =
        mutableMapOf<String, List<Point>>().apply {
            payments?.groupBy { it.category }?.onEach { entry ->
                put(
                    entry.key,
                    entry.value.map { Point(it.time.toInt(), it.amount) }
                )
            }
        }

}