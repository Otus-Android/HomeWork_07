package otus.homework.customview

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import otus.homework.customview.chart.LineChartView
import otus.homework.customview.chart.PieChartView

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var titleView: TextView
    private lateinit var sumView: TextView
    private lateinit var pieChart: PieChartView
    private lateinit var lineChart: LineChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        titleView = findViewById(R.id.dataTitle)
        sumView = findViewById(R.id.dataSum)
        pieChart = findViewById(R.id.pieChart)
        lineChart = findViewById(R.id.lineChart)

        pieChart.setOnSelect { id, color ->
            viewModel.changeSelected(id, color)
        }

        lifecycleScope.launch {
            launch {
                viewModel.labelsData.collectLatest { data ->
                    titleView.text = data.title
                    sumView.text = data.sum
                }
            }
            launch {
                viewModel.pieData.collectLatest {
                    pieChart.populate(it)
                }
            }
            launch {
                viewModel.lineData.collectLatest {
                    lineChart.populate(it, viewModel.currentColor.value)
                }
            }
            launch {
                viewModel.currentColor.collectLatest {
                    lineChart.populate(viewModel.lineData.value, it)
                }
            }
        }
    }

}