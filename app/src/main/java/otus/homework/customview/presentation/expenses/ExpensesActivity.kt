package otus.homework.customview.presentation.expenses

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import otus.homework.customview.R
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.presentation.line.LineChartFragment
import otus.homework.customview.presentation.pie.PieChartFragment

class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpensesViewModel by viewModels { ExpensesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.refreshImageButton.setOnClickListener { viewModel.loadExpenses() }
        binding.sourceCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSourceChanged(isChecked)
        }

        binding.maxEditText.doAfterTextChanged { text -> viewModel.onMaxChanged(text.toString()) }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.pie_chart_menu_item -> PieChartFragment.newInstance().replace()
                R.id.line_chart_menu_item -> LineChartFragment.newInstance().replace()
            }
            true
        }

        if (savedInstanceState == null) {
            viewModel.loadExpenses()
        }
    }

    private fun Fragment.replace() = supportFragmentManager.beginTransaction()
        .replace(R.id.charts_container, this)
        .commit()
}