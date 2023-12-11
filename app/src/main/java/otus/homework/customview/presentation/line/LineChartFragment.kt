package otus.homework.customview.presentation.line

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
import otus.homework.customview.databinding.FragmentLineChartBinding
import otus.homework.customview.presentation.expenses.ExpensesUiState
import otus.homework.customview.presentation.expenses.ExpensesViewModel

/**
 * `Fragment` отображения линейного графика данных по категориям расходов
 */
class LineChartFragment : Fragment() {

    private var _binding: FragmentLineChartBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: ExpensesViewModel by viewModels({ requireActivity() }) { ExpensesViewModel.Factory }
    private val viewModel: LineChartViewModel by viewModels { LineChartViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLineChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.debugCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDebugChanged(isChecked)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.uiState.filterIsInstance(ExpensesUiState.Success::class)
                        .collect { viewModel.process(it.categories) }
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        uiState.current?.let { binding.lineChartView.render(it) }
                        binding.lineChartView.isDebugModeEnabled = uiState.isDebugEnabled

                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        /** Создать новый `fragment` отображения линейного графика [LineChartFragment] */
        fun newInstance() = LineChartFragment()
    }
}