package otus.homework.customview.presentation.line.chart.models

import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.line.chart.converters.LineAreaNodeConverter
import kotlin.random.Random

class LineDataProvider(
    private val areaProvider: LineAreaProvider,
    private val converter: LineAreaNodeConverter = LineAreaNodeConverter()
) {

    private val random = Random

    private var origin = LineData()
    private val nodes = mutableListOf<LineAreaNode>()

    var currentLineX = DEFAULT_LINE_X

    fun getCurrentLineX() = currentLineX.takeIf { it != DEFAULT_LINE_X }

    fun updateCurrentLineX(x: Float, y: Float): Boolean = if (areaProvider.local.contains(x, y)) {
        currentLineX = x
        true
    } else {
        false
    }

    fun clearCurrentLineX() {
        currentLineX = DEFAULT_LINE_X
    }

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
    fun getCurrentNode() = nodes.findLast { it.x < currentLineX }

    fun getNodes(): List<LineAreaNode> = nodes

    private companion object {
        const val DEFAULT_LINE_X = -1f
    }
}
