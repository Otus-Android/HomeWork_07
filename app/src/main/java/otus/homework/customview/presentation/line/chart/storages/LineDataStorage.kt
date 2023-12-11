package otus.homework.customview.presentation.line.chart.storages

import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.line.chart.converters.LineAreaNodeConverter
import otus.homework.customview.presentation.line.chart.models.LineAreaNode

internal class LineDataStorage(
    private val areaProvider: LineAreaStorage,
    private val converter: LineAreaNodeConverter = LineAreaNodeConverter()
) {

    private var origin = LineData()
    private val nodes = mutableListOf<LineAreaNode>()

    fun update(data: LineData) {
        origin = data

        val areaNodes = converter.convert(data.nodes, areaProvider.chart)

        nodes.clear()
        nodes.addAll(areaNodes)
    }

    fun reupdate() = update(origin)

    fun getNodeByX(x: Float) = nodes.findLast { it.x < x }

    fun getNodes(): List<LineAreaNode> = nodes
}
