package otus.homework.customview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val service = Service()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categoryGraphView = findViewById<CategoryGraphView>(R.id.category_graph_view)
        val customView = findViewById<PieChartView>(R.id.pie_chart_view).apply {
            onOrderClick = { category ->
                categoryGraphView.setCategory(category)
                categoryGraphView.visibility = View.VISIBLE
            }
        }

    }
}