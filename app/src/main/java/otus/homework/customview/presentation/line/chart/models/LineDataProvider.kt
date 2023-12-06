package otus.homework.customview.presentation.line.chart.models

import android.graphics.Color
import otus.homework.customview.domain.Expense
import java.util.Date
import kotlin.random.Random

class LineDataProvider(private val areaProvider: LineAreaProvider) {

    private val random = Random

    private val nodes = mutableListOf<LineNode>()

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

    fun calculatePrevious(dataFrame: List<Expense>) {
        // высота
        val baseHeight = areaProvider.local.height()

        // шаг по Y: высоту делим на сумму
        val scaleY =
            areaProvider.local.height() / (dataFrame.maxOfOrNull { it.amount }?.toFloat() ?: 1f)

        val timelineMinOfX = dataFrame.minOfOrNull { it.time } ?: 1L
        val timelineMaxOfX = dataFrame.maxOfOrNull { it.time } ?: 1L
        // TODO: may be 0
        val timelineSpaceOfX = timelineMaxOfX - timelineMinOfX
        val scaleX = areaProvider.local.width() / (timelineSpaceOfX)

        val newValues = mutableListOf<LineNode>()

        dataFrame.forEach {
            val newX = areaProvider.local.left + (it.time - timelineMinOfX) * scaleX
            val newY = areaProvider.local.top + baseHeight - (it.amount * scaleY)
            newValues.add(
                LineNode(
                    x = newX,
                    y = newY,
                    label = it.category,
                    color = Color.argb(
                        255, random.nextInt(256), random.nextInt(256), random.nextInt(256)
                    ),
                    date = Date(it.time)
                )
            )
        }

        nodes.clear()
        nodes.addAll(newValues.sortedBy { it.x })
    }

    fun getNodeByX(x: Float) = nodes.findLast { it.x < x }
    fun getCurrentNode() = nodes.findLast { it.x < currentLineX }

    fun getNodes(): List<LineNode> = nodes

    private companion object {
        const val DEFAULT_LINE_X = -1f
    }
}
