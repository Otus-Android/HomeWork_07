package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pie = findViewById<PieChartView>(R.id.pieChart)
        pie.setExpenses(loadExpenses())

        pie.listener = object : PieChartView.OnSliceClickListener {
            override fun onCategoryClick(category: String) {
                Toast.makeText(this@MainActivity, category, Toast.LENGTH_SHORT).show()
            }
        }
    }
}