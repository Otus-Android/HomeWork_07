package otus.homework.customview.presentation.pie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import otus.homework.customview.R
import otus.homework.customview.databinding.FragmentPieChartBinding
import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.expenses.ExpensesUiState
import otus.homework.customview.presentation.expenses.ExpensesViewModel
import otus.homework.customview.presentation.pie.chart.PieChartView

/**
 * `Fragment` отображения кругового графика данных по категориям расходов
 */
class PieChartFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: ExpensesViewModel by viewModels({ requireActivity() }) { ExpensesViewModel.Factory }
    private val viewModel: PieChartViewModel by viewModels { PieChartViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.debugCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDebugChanged(isChecked)
        }

        binding.styleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onStyleChanged(isChecked)
        }

        binding.pieChartView.sectorTapListener = createPieSectorTapListener()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    sharedViewModel.uiState.filterIsInstance(ExpensesUiState.Success::class)
                        .collect { viewModel.process(it.categories) }
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        binding.pieChartView.render(uiState.data)
                        binding.pieChartView.style = uiState.style
                        binding.pieChartView.isDebugModeEnabled = uiState.isDebugEnabled
                    }
                }
            }
        }
    }

    private fun createPieSectorTapListener() = object : PieChartView.PieSectorTapListener {
        override fun onDown(payload: Any?) {
            (payload as? Category)?.let { category ->
                binding.categoryTextView.text = resources.getString(
                    R.string.pie_chart_sector_category, category.name, category.amount
                )
            }
        }

        override fun onUp(payload: Any?) {
            binding.categoryTextView.text = EMPTY
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        const val EMPTY = ""

        /** Создать новый `fragment` отображения кругового графика [PieChartFragment] */
        fun newInstance() = PieChartFragment()
    }
}