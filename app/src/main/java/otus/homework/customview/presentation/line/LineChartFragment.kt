package otus.homework.customview.presentation.line

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLineChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = mutableListOf<String>()
        val dataAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = dataAdapter

        binding.categorySpinner.onItemSelectedListener = object : DefaultOnItemSelectedListener {
            override fun onItemSelected(position: Int) = viewModel.onCategorySelected(position)
        }

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
                        uiState.lineData?.let { binding.lineChartView.render(it) }
                        binding.lineChartView.isDebugModeEnabled = uiState.isDebugEnabled
                        dataAdapter.clear()
                        dataAdapter.addAll(uiState.categories.map { it.name })
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private interface DefaultOnItemSelectedListener : OnItemSelectedListener {


        fun onItemSelected(position: Int) {
            /* do nothing */
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            /* do nothing */
        }
    }

    companion object {

        /** Создать новый `fragment` отображения линейного графика [LineChartFragment] */
        fun newInstance() = LineChartFragment()
    }
}