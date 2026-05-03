package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {
    private val viewModel: PieViewModel by viewModels {
        PieViewModelFactory(context = this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChartView = findViewById<PieChart>(R.id.pieChartView)
        val contentNameTextView = findViewById<TextView>(R.id.categoryName)

        val data = viewModel.data
        pieChartView.onSliceClick = { categoryName ->
            contentNameTextView.text = categoryName
        }

        pieChartView.setData(data)
    }
}