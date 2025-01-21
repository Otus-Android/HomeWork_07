package otus.homework.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PieChartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pieChart = view.findViewById<PieChartView>(R.id.pie_chart)
        pieChart.setData(ExpensesRepository(requireContext()).getExpenses())
        pieChart.setOnCategoryClickListener(object : PieChartView.OnCategoryClickListener {
            override fun onClick(categoryWithColor: Pair<String, Int>) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.main_container,
                        LineChartFragment.newInstance(categoryWithColor.first, categoryWithColor.second)
                    )
                    .addToBackStack(null)
                    .commit()
            }

        })
    }
}