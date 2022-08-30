package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        val pieChartClickListener = object : PieChartClickListener {
            override fun onClick(category: String) {
                Log.d("CLICKLISTENER", category)
            }
        }

        val pieChart = findViewById<PieChartView>(R.id.pieChart)
        pieChart.setData(data)
        pieChart.pieChartClickListener = pieChartClickListener
    }

    private fun getMarketData(): List<MarketData> = readRawJson(R.raw.payload)

    private inline fun <reified T> readRawJson(@RawRes rawResId: Int): T {
        resources.openRawResource(rawResId).bufferedReader().use {
            return gson.fromJson<T>(it, object: TypeToken<T>() {}.type)
        }
    }
}