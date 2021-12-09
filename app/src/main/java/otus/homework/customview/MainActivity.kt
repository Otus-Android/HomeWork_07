package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import otus.homework.customview.paichart.PieChartView


class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_chart_view)

        savedInstanceState ?: setupPieChart()
    }

    private fun setupPieChart() {
        val payload: String =
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }

        val payments = Gson().fromJson(payload, Array<Payment>::class.java).toList()
        val total = payments.sumBy { it.amount }

        val paymentsByCategory = payments.groupingBy { it.category }
            .aggregateTo(mutableMapOf()) { _, accumulator: Int?, el, first ->
                if (first) el.amount else accumulator!!.plus(el.amount)
            }.mapTo(mutableListOf()) {
                Pair(it.key, it.value * 100f/ total)
            }

        pieChart.setData(total, paymentsByCategory)
        pieChart.onClickListener = { category ->
            Log.d(this::class.simpleName, "onPieChartCategoryClicked: $category")
        }
    }
}

