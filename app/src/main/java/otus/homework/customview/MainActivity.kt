package otus.homework.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import otus.homework.customview.piechartview.Category
import otus.homework.customview.piechartview.ClickGestureListener
import otus.homework.customview.piechartview.DemoDataSource
import otus.homework.customview.piechartview.PieChartView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChart = findViewById<PieChartView>(R.id.pieChart)

        val data = DemoDataSource().getData(this)
        pieChart.setData(data)

        val pieChartGestureListener = ClickGestureListener<Category>(pieChart) { category ->
            Toast.makeText(this, "You clicked category: $category!", Toast.LENGTH_SHORT).show()
        }
        val pieChartGestureDetector = GestureDetectorCompat(this, pieChartGestureListener)
        pieChartGestureDetector.setIsLongpressEnabled(false)
        pieChart.setOnTouchListener(pieChartGestureDetector, pieChartGestureListener)
    }
}
