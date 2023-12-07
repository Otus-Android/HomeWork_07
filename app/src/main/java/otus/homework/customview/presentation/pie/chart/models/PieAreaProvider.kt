package otus.homework.customview.presentation.pie.chart.models

import otus.homework.customview.presentation.pie.chart.PieData
import kotlin.random.Random

class PieAreaProvider {

    private val random = Random

    private var pieAngleNodes = emptyList<PieAreaNode>()

    fun calculate(pieData: PieData) {
        val generalAmount = pieData.nodes.sumOf { it.value.toDouble() }

        var count = 0f
        val newValues2 = pieData.nodes.map { (value, label, color) ->
            PieAreaNode(
                label = label.orEmpty(),
                startAngle = count,
                sweepAngle = value / (generalAmount.toFloat()) * 360f,
                color = color
            ).also { angleNode ->
                count += angleNode.sweepAngle
            }
        }

        pieAngleNodes = newValues2.sortedBy { it.startAngle }
    }


    fun getNodes(): List<PieAreaNode> = pieAngleNodes

    fun getCategory(angle: Float) =
        pieAngleNodes.find { it.startAngle <= angle && it.startAngle + it.sweepAngle > angle }?.label


    fun getNode(angle: Float) =
        pieAngleNodes.find { it.startAngle <= angle && it.startAngle + it.sweepAngle > angle }
}