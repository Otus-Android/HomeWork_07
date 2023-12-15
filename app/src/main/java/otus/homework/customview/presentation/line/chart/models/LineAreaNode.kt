package otus.homework.customview.presentation.line.chart.models

import java.util.Calendar

/**
 * Внутренняя модель узла линейного графика
 *
 * @param x координата по оси X
 * @param y координата по оси Y
 * @param label подпись
 * @param calendar дата, соответствующая координте [y]
 */
internal data class LineAreaNode(
    val x: Float,
    val y: Float,
    val label: String?,
    val calendar: Calendar,
)