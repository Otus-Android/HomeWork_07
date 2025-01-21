package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

private const val CATEGORY = "category"
private const val COLOR = "color"

class LineChartFragment : Fragment() {
    private var category: String? = null
    private var color: Int = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(CATEGORY)
            color = it.getInt(COLOR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_line_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lineChart = view.findViewById<LineChartView>(R.id.line_chart)
        category?.let {
            lineChart.setDataAndColor(
                ExpensesRepository(requireContext()).getExpensesByCategory(it),
                color
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(category: String, color: Int) =
            LineChartFragment().apply {
                arguments = Bundle().apply {
                    putString(CATEGORY, category)
                    putInt(COLOR, color)
                }
            }
    }
}