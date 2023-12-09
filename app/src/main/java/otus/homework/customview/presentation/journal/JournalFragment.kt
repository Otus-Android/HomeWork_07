package otus.homework.customview.presentation.journal

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
import otus.homework.customview.databinding.FragmentJournalBinding
import otus.homework.customview.presentation.expenses.ExpensesUiState
import otus.homework.customview.presentation.expenses.ExpensesViewModel

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: ExpensesViewModel by viewModels({ requireActivity() }) { ExpensesViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = JournalAdapter()

        val margin = resources.getDimension(R.dimen.item_journal_margin)
        binding.journalRecyclerView.adapter = adapter
        binding.journalRecyclerView.addItemDecoration(MarginItemDecorator(margin.toInt()))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.uiState.filterIsInstance(ExpensesUiState.Success::class)
                    .collect { adapter.submitList(it.expenses) }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = JournalFragment()
    }
}