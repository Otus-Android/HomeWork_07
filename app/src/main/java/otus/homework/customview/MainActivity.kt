package otus.homework.customview

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import otus.homework.customview.chart.PieChartView
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChartView>(R.id.pieChart)
        val titleView = findViewById<TextView>(R.id.dataTitle)
        val descriptionView = findViewById<TextView>(R.id.dataDescription)
        val sumView = findViewById<TextView>(R.id.dataSum)
        val dateView = findViewById<TextView>(R.id.dataDate)

        pieChart.setOnSelect { id, color ->
            if (id == null) {
                // ничего не выбрано
                titleView.text = "Tap"
                descriptionView.text = "on pie"
                sumView.text = "and see"
                dateView.text = "N/A"
                // TODO: очистить второй чарт
            } else {
                // есть id
                viewModel.chartData.value.find {
                    it.id == id
                }?.also {
                    // текст в UI
                    titleView.text = it.name
                    descriptionView.text = it.category
                    sumView.text = it.amount.toString()
                    dateView.text = Date(it.time).toString()
                    // TODO: заполнить второй чарт
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chartData.collectLatest { list ->
                pieChart.populate(list)
            }
        }
    }

}