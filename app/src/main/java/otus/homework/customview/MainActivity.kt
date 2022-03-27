package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import otus.homework.customview.graph.GraphView
import otus.homework.customview.paichart.PieChartView


class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChartView
    private lateinit var graph: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_chart_view)
        graph = findViewById(R.id.graph_view)

        setupPieChart()
    }

    private fun setupPieChart() {
        val payload: String =
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }

        val payments = Gson().fromJson(payload, Array<Payment>::class.java).toList()
        val total = payments.sumBy { it.amount }

        val byCategory = payments.groupBy { it.category }

        val pieChartData = byCategory.mapTo(mutableListOf()) { category ->
            Pair(category.key, category.value.sumOf { it.amount } * 100f/ total )
        }

        pieChart.setData(total, pieChartData)
        pieChart.onClickListener = { category ->
            byCategory[category]?.let { list -> graph.setData(list.associate { it.date to it.amount }) }
        }
    }
}

