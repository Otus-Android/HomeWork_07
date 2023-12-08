package otus.homework.customview.presentation.pie.chart.converters

import otus.homework.customview.presentation.pie.chart.PieNode
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode

class PieAresNodeConverter {

    fun convert(nodes: List<PieNode>): List<PieAreaNode> {
        val generalAmount = nodes.sumOf { it.value.toDouble() }

        var count = 0f
        val newValues2 = nodes.map { (value, label, color) ->
            PieAreaNode(
                label = label.orEmpty(),
                startAngle = count,
                sweepAngle = value / (generalAmount.toFloat()) * 360f,
                color = color
            ).also { angleNode ->
                count += angleNode.sweepAngle
            }
        }

        return newValues2.sortedBy { it.startAngle }
    }
}