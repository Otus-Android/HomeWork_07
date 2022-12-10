package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.pie_chart.PieChart

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart: PieChart = findViewById(R.id.pie_chart)
        pieChart.setOnChartClickListener { items ->
            Log.d("MAIN_ACTIVITY", items.toString())
        }
    }
}