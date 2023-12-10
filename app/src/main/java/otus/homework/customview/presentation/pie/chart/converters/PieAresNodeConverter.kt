package otus.homework.customview.presentation.pie.chart.converters

import otus.homework.customview.presentation.pie.chart.PieNode
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode

/**
 * Конвертер внутренних моделей узлов кругового графика
 */
internal class PieAresNodeConverter {

    /** Преобразовать список [PieNode] в список [PieAreaNode] */
    fun convert(nodes: List<PieNode>): List<PieAreaNode> {
        val totalAmount = nodes.sumOf { it.value.toDouble() }.toFloat()

        var startAngle = 0f
        val pieAreaNodes = nodes.map { (value, label, color) ->
            PieAreaNode(
                startAngle = startAngle,
                sweepAngle = value / (totalAmount) * CIRCLE_DEGREES,
                label = label,
                color = color
            ).also { angleNode -> startAngle += angleNode.sweepAngle }
        }

        return pieAreaNodes.sortedBy { it.startAngle }
    }

    private companion object {

        /** Кол-во градусов в круге */
        const val CIRCLE_DEGREES = 360f
    }
}