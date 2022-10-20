package otus.homework.customview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import otus.homework.customview.Either
import otus.homework.customview.tools.JsonParser
import otus.homework.customview.R
import otus.homework.customview.databinding.FragmentPieChartBinding
import otus.homework.customview.entities.Category
import otus.homework.customview.recycler_view.CategoryListAdapter

class PieChartFragment : Fragment(), HasTitle {

    private lateinit var listAdapter: CategoryListAdapter

    private var _binding: FragmentPieChartBinding? = null
    private val binding: FragmentPieChartBinding
        get() = _binding ?: throw RuntimeException("FragmentPieChartBinding is null")

    private val categoryClickListener: (Category) -> Unit = { category ->
        Toast.makeText(requireContext(), category.name, Toast.LENGTH_SHORT).show()
        val destinationId = R.id.timelineFragment
        val args = TimelineFragment.createArgs(category.name, category.color)
        findNavController().navigate(destinationId, args)
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
        refreshPieChartView(binding.pieChartView)

        binding.buttonRefresh.setOnClickListener {
            refreshPieChartView(binding.pieChartView)
        }
        binding.pieChartView.setOnClickListener(categoryClickListener)
    }

    private fun refreshPieChartView(view: PieChartView) {
        when (val categories = JsonParser(requireContext()).sortedCategoriesByTotal()) {
            is Either.Success -> {
                view.initCategories(categories.result)
                setUpRecyclerView(categories.result)
            }
            is Either.Failure -> Toast.makeText(
                requireContext(),
                categories.error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpRecyclerView(list: List<Category>) {
        listAdapter = CategoryListAdapter()
        binding.recyclerView.adapter = listAdapter
        listAdapter.submitList(list)
        listAdapter.onListClickListener = categoryClickListener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun getTitle(): String = getString(R.string.pie_chart)
}