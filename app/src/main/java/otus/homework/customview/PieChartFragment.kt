package otus.homework.customview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import otus.homework.customview.model.PayloadModel

class PieChartFragment : Fragment() {

    private var payloadModel: Array<PayloadModel>? = null
    private val pieChartData = mutableMapOf<String, Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val payloadRepository = PayloadRepository(context)
        payloadModel = payloadRepository.getData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payloadModel?.forEach {
            if (pieChartData.containsKey(it.category)) {
                val currentAmount = pieChartData[it.category]!!
                pieChartData[it.category] = currentAmount + it.amount
            } else {
                pieChartData[it.category] = it.amount
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChartView = view.findViewById<PieChartView>(R.id.pie_chart_view)

        if (savedInstanceState == null) {
            pieChartView.setData(
                data = pieChartData
            )
        }
        pieChartView.setOnCategoryClickListener { category, amount ->
            val payload = "$category: $amount Руб"
            Toast.makeText(context, payload, Toast.LENGTH_SHORT).show()
        }
    }

}