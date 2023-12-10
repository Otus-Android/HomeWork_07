package otus.homework.customview.presentation.line.chart.converters

import android.graphics.RectF
import otus.homework.customview.presentation.line.chart.LineNode
import otus.homework.customview.presentation.line.chart.models.LineAreaNode
import java.util.Date

internal class LineAreaNodeConverter {

    fun convert(nodes: List<LineNode>, area: RectF): List<LineAreaNode> {
        // высота
        val baseHeight = area.height()

        // шаг по Y: высоту делим на сумму
        val scaleY =
            area.height() / (nodes.maxOfOrNull { it.value } ?: 1f)

        val timelineMinOfX = nodes.minOfOrNull { it.time } ?: 1L
        val timelineMaxOfX = nodes.maxOfOrNull { it.time } ?: 1L
        // TODO: may be 0
        val timelineSpaceOfX = timelineMaxOfX - timelineMinOfX
        val scaleX = area.width() / (timelineSpaceOfX)

        val newValues = mutableListOf<LineAreaNode>()

        nodes.forEach {
            val newX = area.left + (it.time - timelineMinOfX) * scaleX
            val newY = area.top + baseHeight - (it.value * scaleY)
            newValues.add(
                LineAreaNode(
                    x = newX,
                    y = newY,
                    label = it.label,
                    date = Date(it.time)
                )
            )
        }

        return newValues.sortedBy { it.x }
    }
}