package otus.homework.customview.presentation.pie.chart.models

import otus.homework.customview.presentation.pie.chart.PieData
import otus.homework.customview.presentation.pie.chart.converters.PieAresNodeConverter

class PieDataProvider(private val converter: PieAresNodeConverter = PieAresNodeConverter()) {

    private val pieAngleNodes = mutableListOf<PieAreaNode>()

    fun calculate(pieData: PieData) {
        val areaNodes = converter.convert(pieData.nodes)
        pieAngleNodes.clear()
        pieAngleNodes.addAll(areaNodes)
    }


    fun getNodes(): List<PieAreaNode> = pieAngleNodes

    fun getCategory(angle: Float) =
        pieAngleNodes.find { it.startAngle <= angle && it.startAngle + it.sweepAngle > angle }?.label


    fun getNode(angle: Float) =
        pieAngleNodes.find { it.startAngle <= angle && it.startAngle + it.sweepAngle > angle }
}