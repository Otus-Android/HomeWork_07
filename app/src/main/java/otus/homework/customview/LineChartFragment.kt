package otus.homework.customview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import otus.homework.customview.model.PayloadModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LineChartFragment : Fragment() {

    private var payloadModel: Array<PayloadModel>? = null
    private val lineChartData = mutableMapOf<String, Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val payloadRepository = PayloadRepository(context)
        payloadModel = payloadRepository.getData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payloadModel?.forEach {
            val key = convertLongToTime(it.time)
            if (lineChartData.containsKey(key)) {
                val currentAmount = lineChartData[key]!!
                lineChartData[key] = currentAmount + it.amount
            } else {
                lineChartData[key] = it.amount
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_line_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lineChartView = view.findViewById<LineChartView>(R.id.line_chart_view)

        if (savedInstanceState == null) {
            lineChartView.setData(
                data = lineChartData
            )
        }
    }

    private fun convertLongToTime(time: Int): String {
        val date = Date(time * 1000L)
        val format = SimpleDateFormat("dd.MM", Locale.getDefault())
        return format.format(date)
    }
}