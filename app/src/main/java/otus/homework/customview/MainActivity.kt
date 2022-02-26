package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import otus.homework.customview.data.pie.OnSegmentClickListener
import otus.homework.customview.view.AnimatedLineGraph
import otus.homework.customview.view.CustomPieChart

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val textField = findViewById<TextView>(R.id.category_info)
    val graph = findViewById<AnimatedLineGraph>(R.id.line_graph)

    findViewById<CustomPieChart>(R.id.pie_chart).apply {
      setChartData()
      setOnSegmentClickListener(object : OnSegmentClickListener {
        override fun action(category: String, amount: Int) {
          textField.text = category
          graph.setGraphData(category)
        }
      })
    }
  }
}