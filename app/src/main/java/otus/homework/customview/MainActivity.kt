package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.sealed.Mode.DetailsCategory
import otus.homework.customview.sealed.Mode.ExpensesCategory
import otus.homework.customview.sealed.Result.Error
import otus.homework.customview.sealed.Result.Expenses

class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding

    private val pieChartView: ChartView
    get() = _binding.vPieChart

    private val detailsCategoryView: DetailsView
    get() = _binding.vDetails

    private val chartViewModel by lazy {
        ViewModelProvider(this)[ChartViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        pieChartView.onSectorClickListener = object : ChartView.OnSectorClickListener {
            override fun onClick(category: String, color: Int) {
                chartViewModel.showDetailsCategory(category, color)
            }
        }

        chartViewModel.getExpenseData(this)

        chartViewModel.parseExpensesResult.observe(this) {
            when(it) {
                is Expenses -> chartViewModel.showExpensesByCategory(it.allExpenses)
                is Error -> Log.d("MainActivity", "${it.throwable}")
            }
        }

        chartViewModel.mode.observe(this) {
            when(it) {
                is ExpensesCategory -> pieChartView.populate(it.sectorsByCategory)
                is DetailsCategory -> detailsCategoryView.populate(it.detailsData)
                else -> {}
            }
        }
    }

    override fun onBackPressed() {
        when(detailsCategoryView.isOpened) {
            true -> {
                detailsCategoryView.close()
                pieChartView.open()
            }
            false -> super.onBackPressed()
        }
    }
}