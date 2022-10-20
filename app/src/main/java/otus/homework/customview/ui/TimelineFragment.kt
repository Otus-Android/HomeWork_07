package otus.homework.customview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import otus.homework.customview.Either
import otus.homework.customview.tools.JsonParser
import otus.homework.customview.databinding.FragmentTimelineBinding
import otus.homework.customview.entities.Spending
import otus.homework.customview.recycler_view.SpendingListAdapter

class TimelineFragment : Fragment(), HasTitle {

    private lateinit var listAdapter: SpendingListAdapter

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
        refreshTimelineView(binding.timelineView)

        binding.buttonRefresh.setOnClickListener {
            refreshTimelineView(binding.timelineView)
        }

    }

    private fun refreshTimelineView(view: TimelineView) {
        when (val spendingList =
            JsonParser(requireContext()).sortedSpendingListByTime(categoryName)) {
            is Either.Success -> {
                view.initSpendingList(spendingList.result)
                view.initCategoryColor(categoryColor)
                setUpRecyclerView(spendingList.result)
                view.requestLayout()
                view.invalidate()
            }
            is Either.Failure -> Toast.makeText(
                requireContext(),
                spendingList.error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpRecyclerView(list: List<Spending>) {
        listAdapter = SpendingListAdapter()
        binding.recyclerView.adapter = listAdapter
        listAdapter.submitList(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        private const val CATEGORY_NAME = "category name"
        private const val CATEGORY_COLOR = "category color"

        fun createArgs(categoryName: String, categoryColor: Int) =
            Bundle().apply {
                putString(CATEGORY_NAME, categoryName)
                putInt(CATEGORY_COLOR, categoryColor)
            }
    }

    override fun getTitle(): String = categoryName
}