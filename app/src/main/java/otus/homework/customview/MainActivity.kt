package otus.homework.customview

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import otus.homework.customview.line_graph.LineGraphView
import otus.homework.customview.piechart.PieChartView

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView: PieChartView = findViewById(R.id.pie_chart)
        pieChartView.setOnChartClickListener { items ->
            Log.d("MAIN_ACTIVITY", items.toString())
        }

        val lineGraphView: LineGraphView = findViewById(R.id.line_graph_view)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            if (button.text == "Show Pie Chart") {
                button.text = "Show Line Graph"
                pieChartView.visibility = View.VISIBLE
                lineGraphView.visibility = View.INVISIBLE
            } else {
                button.text = "Show Pie Chart"
                pieChartView.visibility = View.INVISIBLE
                lineGraphView.visibility = View.VISIBLE
            }
        }
    }
}