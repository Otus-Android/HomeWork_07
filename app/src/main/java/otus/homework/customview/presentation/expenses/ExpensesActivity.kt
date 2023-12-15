package otus.homework.customview.presentation.expenses

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import otus.homework.customview.R
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.presentation.journal.JournalFragment
import otus.homework.customview.presentation.line.LineChartFragment
import otus.homework.customview.presentation.pie.PieChartFragment

/**
 * `Ativity` данных по расходам
 */
class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpensesViewModel by viewModels { ExpensesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.refreshImageButton.setOnClickListener { viewModel.updateExpenses() }
        binding.sourceCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSourceChanged(isChecked)
        }

        binding.maxEditText.doAfterTextChanged { text -> viewModel.onMaxChanged(text.toString()) }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.pie_chart_menu_item -> PieChartFragment.newInstance().replace(PIE_TAG)
                R.id.line_chart_menu_item -> LineChartFragment.newInstance().replace(LINE_TAG)
                R.id.journal_menu_item -> JournalFragment.newInstance().replace(JOURNAL_TAG)
            }
            true
        }

        if (savedInstanceState == null) {
            viewModel.loadExpenses()
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        ExpensesUiState.Idle -> {
                            binding.chartsContainer.visibility = View.GONE
                            binding.loadingContainer.visibility = View.VISIBLE
                            binding.loadingProgressBar.visibility = View.GONE
                            binding.errorTextView.visibility = View.GONE
                        }

                        ExpensesUiState.Loading -> {
                            binding.chartsContainer.visibility = View.GONE
                            binding.loadingContainer.visibility = View.VISIBLE
                            binding.loadingProgressBar.visibility = View.VISIBLE
                            binding.errorTextView.visibility = View.GONE
                        }

                        is ExpensesUiState.Success -> {
                            binding.loadingContainer.visibility = View.GONE
                            binding.chartsContainer.visibility = View.VISIBLE
                        }

                        is ExpensesUiState.Error -> {
                            binding.chartsContainer.visibility = View.GONE
                            binding.loadingProgressBar.visibility = View.GONE
                            binding.errorTextView.visibility = View.VISIBLE
                            binding.errorTextView.text = uiState.message
                        }
                    }
                }
            }
        }
    }

    private fun Fragment.replace(tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag) != null) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.charts_container, this, tag)
            .commit()
    }

    private companion object {
        const val PIE_TAG = "PIE_TAG"
        const val LINE_TAG = "LINE_TAG"
        const val JOURNAL_TAG = "JOURNAL_TAG"
    }
}