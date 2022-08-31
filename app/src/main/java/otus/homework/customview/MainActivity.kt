package otus.homework.customview

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RawRes
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder

class MainActivity : AppCompatActivity() {
    private val data = PieViewData()
    private val gson by lazy { GsonBuilder().create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val marketData = getMarketData()
        marketData.forEach { data.add(it.category, it.amount.toDouble()) }
        val pointList = mapPurchasesToPoints(marketData)
        val pieChart = findViewById<PieChartView>(R.id.pieChart)
        val graph = findViewById<GraphView>(R.id.graph)

        val pieChartClickListener = object : PieChartClickListener {
            override fun onClick(category: String) {
                graph.setData(pointList[category]!!)
            }
        }

        pieChart.setData(data)
        pieChart.pieChartClickListener = pieChartClickListener
    }

    private fun getMarketData(): List<MarketData> = readRawJson(R.raw.payload)

    private inline fun <reified T> readRawJson(@RawRes rawResId: Int): T {
        resources.openRawResource(rawResId).bufferedReader().use {
            return gson.fromJson<T>(it, object: TypeToken<T>() {}.type)
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