package otus.homework.customview.ui

import alektas.views.line_chart.LineChart
import alektas.views.line_chart.LineChartDataSet
import alektas.views.line_chart.LineChartPoint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import otus.homework.customview.R
import otus.homework.customview.data.models.Purchase
import otus.homework.customview.ui.utils.ColorUtils

class LineChartFragment : Fragment(R.layout.fragment_line_chart) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val purchases = (requireActivity() as MainActivity).purchases.values.flatten()
        val chartPoints = purchases.mapToLineChartPoints()

        view.findViewById<LineChart>(R.id.lineChart).apply {
            dataSet = LineChartDataSet(
                id = 0,
                label = "Purchases",
                points = chartPoints,
                color = ColorUtils.randomColor()
            )
        }
    }

    private fun List<Purchase>.mapToLineChartPoints(): List<LineChartPoint> =
        sortedBy { it.id }
            .mapIndexed { i, purchase ->
                val dayNumber = i + 1f
                LineChartPoint(
                    id = purchase.id,
                    label = purchase.name,
                    valueY = purchase.amount.toFloat(),
                    valueX = dayNumber
                )
            }
}