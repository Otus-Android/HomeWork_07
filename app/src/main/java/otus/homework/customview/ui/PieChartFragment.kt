package otus.homework.customview.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import otus.homework.customview.Either
import otus.homework.customview.R
import otus.homework.customview.databinding.FragmentPieChartBinding
import otus.homework.customview.entities.Category
import otus.homework.customview.recycler_view.CategoryListAdapter
import otus.homework.customview.tools.JsonParser

class PieChartFragment : Fragment(), HasTitle {

    private lateinit var listAdapter: CategoryListAdapter
    private val categories: MutableList<Category> = mutableListOf()

    private var _binding: FragmentPieChartBinding? = null
    private val binding: FragmentPieChartBinding
        get() = _binding ?: throw RuntimeException("FragmentPieChartBinding is null")

    private val categoryClickListener: (Category) -> Unit = { category ->
        val destinationId = R.id.timelineFragment
        val args = TimelineFragment.createArgs(category.name, category.color)
        findNavController().navigate(destinationId, args)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_CATEGORIES, PieChartFragmentSaveState(categories))
        _binding?.pieChartView?.isFragmentStateSaved = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        @Suppress("DEPRECATION")
        val saveState =
            if (Build.VERSION.SDK_INT >= 33)
                savedInstanceState?.getParcelable(
                    KEY_CATEGORIES,
                    PieChartFragmentSaveState::class.java
                )
            else savedInstanceState?.getParcelable(KEY_CATEGORIES)
        saveState?.let { restoreFromSaveState(it) } ?: loadFromJson()

        binding.buttonRefresh.setOnClickListener {
            loadFromJson()
            binding.pieChartView.runAnimation()
        }
        binding.pieChartView.setOnClickListener(categoryClickListener)
    }

    private fun setUpRecyclerView() {
        listAdapter = CategoryListAdapter()
        binding.recyclerView.adapter = listAdapter
        listAdapter.submitList(categories)
        listAdapter.onListClickListener = categoryClickListener
    }

    private fun restoreFromSaveState(saveState: PieChartFragmentSaveState) {
        if (saveState.categories.size != 0) {
            updateCategories(saveState.categories)
            binding.pieChartView.isFragmentStateSaved = true
        } else loadFromJson()

    }

    private fun loadFromJson() {
        when (val sortedCategories = JsonParser(requireContext()).sortedCategoriesByTotal()) {
            is Either.Success -> {
                updateCategories(sortedCategories.result)
                binding.pieChartView.initView(categories)
            }
            is Either.Failure -> Toast.makeText(
                requireContext(),
                sortedCategories.error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCategories(list: List<Category>) {
        categories.clear()
        categories.addAll(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun getTitle(): String = getString(R.string.pie_chart)

    companion object {

        private const val KEY_CATEGORIES = "categories"
    }
}