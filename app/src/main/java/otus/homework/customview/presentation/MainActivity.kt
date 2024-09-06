package otus.homework.customview.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import otus.homework.customview.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<CustomViewViewModel>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observePieChartAngles()
        setPayloadCategorySelectionListener()
    }

    private fun observePieChartAngles() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pieChartAngles.collect {
                    binding.pieChartView.setPieChartAngles(it)
                }
            }
        }
    }

    private fun setPayloadCategorySelectionListener() {
        binding.pieChartView.setSelectionListener { selectedCategory ->
            Toast.makeText(this, selectedCategory, Toast.LENGTH_LONG).show()

            binding.expensesGraphView.setMaxDailyExpenseOfAllCategories(viewModel.getMaxDailyExpenseOfAllCategories())
            binding.expensesGraphView.setDaysToExpenses(viewModel.getDaysToExpenses(selectedCategory))
        }
    }
}