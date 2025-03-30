package otus.homework.customview.presentation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.otus.homework.R
import kotlinx.coroutines.launch
import otus.homework.customview.presentation.firstPieChart.PieChartView

class MainActivity : AppCompatActivity() {
    private val pieChartViewModel: PieChartViewModel by viewModels { PieChartViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customDiagram = findViewById<PieChartView>(R.id.pi_chart)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pieChartViewModel.spends.collect {
                    customDiagram.setData(it)
                }
            }
        }
    }
}