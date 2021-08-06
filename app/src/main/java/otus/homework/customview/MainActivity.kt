package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv = findViewById<TextView>(R.id.help_text)
        val categoryGraphView = findViewById<CategoryGraphView>(R.id.category_graph_view)
        findViewById<PieChartView>(R.id.pie_chart_view).apply {
            onOrderClick = { category ->
                tv.visibility = View.GONE
                categoryGraphView.setCategory(category)
                categoryGraphView.visibility = View.VISIBLE
            }
        }
    }
}