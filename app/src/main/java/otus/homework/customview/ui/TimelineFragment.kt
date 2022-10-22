package otus.homework.customview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import otus.homework.customview.Either
import otus.homework.customview.databinding.FragmentTimelineBinding
import otus.homework.customview.entities.Spending
import otus.homework.customview.recycler_view.SpendingListAdapter
import otus.homework.customview.tools.JsonParser

class TimelineFragment : Fragment(), HasTitle {

    private lateinit var listAdapter: SpendingListAdapter
    private val spendingList: MutableList<Spending> = mutableListOf()

    private var _binding: FragmentTimelineBinding? = null
    private val binding: FragmentTimelineBinding
        get() = _binding ?: throw RuntimeException("FragmentTimelineBinding is null")

    private val categoryName by lazy {
        arguments?.getString(CATEGORY_NAME) ?: throw RuntimeException("Category not defined")
    }
    private val categoryColor by lazy {
        arguments?.getInt(CATEGORY_COLOR) ?: throw RuntimeException("Color not defined")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        val saveState =
            savedInstanceState?.getParcelable(KEY_SPENDING_LIST) as? TimelineFragmentSaveState
        saveState?.let { restoreFromSaveState(it) } ?: loadFromJson()

        binding.buttonRefresh.setOnClickListener { loadFromJson() }
    }

    private fun restoreFromSaveState(saveState: TimelineFragmentSaveState) {
        updateSpendingList(saveState.spendingList)
        binding.timelineView.updateSpendingList(spendingList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_SPENDING_LIST, TimelineFragmentSaveState(spendingList))
    }

    private fun loadFromJson() {
        when (val spendingListResult =
            JsonParser(requireContext()).sortedSpendingListByTime(categoryName)) {
            is Either.Success -> {
                updateSpendingList(spendingListResult.result)
                binding.timelineView.updateSpendingList(spendingList)
                binding.timelineView.initCategoryColor(categoryColor)
            }
            is Either.Failure -> Toast.makeText(
                requireContext(),
                spendingListResult.error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateSpendingList(list: List<Spending>) {
        spendingList.clear()
        spendingList.addAll(list)
    }

    private fun setUpRecyclerView() {
        listAdapter = SpendingListAdapter()
        binding.recyclerView.adapter = listAdapter
        listAdapter.submitList(spendingList)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun getTitle(): String = categoryName

    companion object {
        private const val CATEGORY_NAME = "category name"
        private const val CATEGORY_COLOR = "category color"
        private const val KEY_SPENDING_LIST = "spending list"

        fun createArgs(categoryName: String, categoryColor: Int) =
            Bundle().apply {
                putString(CATEGORY_NAME, categoryName)
                putInt(CATEGORY_COLOR, categoryColor)
            }
    }
}