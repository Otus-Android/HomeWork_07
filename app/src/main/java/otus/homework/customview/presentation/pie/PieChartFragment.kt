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
import otus.homework.customview.databinding.FragmentPieChartBinding
import otus.homework.customview.presentation.expenses.ExpensesUiState
import otus.homework.customview.presentation.expenses.ExpensesViewModel

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

        binding.styleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onStyleChanged(isChecked)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    sharedViewModel.uiState.filterIsInstance(ExpensesUiState.Success::class)
                        .collect { viewModel.load(it.categories) }
                }

                launch {
                    viewModel.uiState.collect {
                        binding.pieChartView.render(it.data)
                        binding.pieChartView.setStyle(it.style)
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
        fun newInstance() = PieChartFragment()
    }
}