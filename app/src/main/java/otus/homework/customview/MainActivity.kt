package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import otus.homework.customview.pie_chart.PayChartView
import otus.homework.customview.pie_chart.Payment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonString = assets.open("payload.json").bufferedReader().use { it.readText() }
        val payments = Json.decodeFromString<List<Payment>>(jsonString)
        findViewById<PayChartView>(R.id.pay_charts).apply {
            setPayments(payments)
            onChartClick = {
                Log.d(this::class.simpleName, "category = $it")
            }
        }
    }
}