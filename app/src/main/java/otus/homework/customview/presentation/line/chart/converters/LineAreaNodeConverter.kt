package otus.homework.customview.presentation.line.chart.converters

import android.graphics.RectF
import otus.homework.customview.presentation.line.chart.LineNode
import otus.homework.customview.presentation.line.chart.models.LineAreaNode
import java.util.Calendar

/**
 * Конвертер внутренних моделей узлов линейного графика
 */
internal class LineAreaNodeConverter {

    /** Преобразовать список [LineNode] в список [LineAreaNode] на основе данных доступной области [area] */
    fun convert(nodes: List<LineNode>, area: RectF): List<LineAreaNode> {
        if (nodes.isEmpty()) return emptyList()

        // коэффициент масшабирования по оси Y
        val maxValue = nodes.maxOf { it.value }
        if (maxValue == 0f) return emptyList()
        val scaleY = area.height() / maxValue

        // коэффициент масшабирования по оси X
        val minTime = nodes.minOf { it.time }
        val maxTime = nodes.maxOf { it.time }
        val timeline = maxTime - minTime
        if (timeline == 0L) return emptyList()
        val scaleX = area.width() / (timeline)

        val lineAreaNodes = nodes.map { node ->
            LineAreaNode(
                x = area.left + (node.time - minTime) * scaleX,
                y = area.top + area.height() - (node.value * scaleY),
                label = node.label,
                calendar = Calendar.getInstance().also { it.timeInMillis = node.time }
            )
        }

        return lineAreaNodes.sortedBy { it.x }
    }
}