package otus.homework.customview.presentation.line.chart.data

import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.line.chart.converters.LineAreaNodeConverter
import otus.homework.customview.presentation.line.chart.models.LineAreaNode
import otus.homework.customview.presentation.line.chart.area.LineAreaProvider

class LineDataProvider(
    private val areaProvider: LineAreaProvider,
    private val converter: LineAreaNodeConverter = LineAreaNodeConverter()
) {

    private var origin = LineData()
    private val nodes = mutableListOf<LineAreaNode>()

    fun calculate(data: LineData) {
        origin = data

        val areaNodes = converter.convert(data.nodes, areaProvider.local)

        nodes.clear()
        nodes.addAll(areaNodes)
    }

    fun recalculate() {
        calculate(origin)
    }


    fun getNodeByX(x: Float) = nodes.findLast { it.x < x }

    fun getNodes(): List<LineAreaNode> = nodes
}
