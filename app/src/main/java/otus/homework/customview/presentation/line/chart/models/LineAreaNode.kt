package otus.homework.customview.presentation.line.chart.models

import java.util.Date

/**
 * Внутренняя модель узла линейного графика
 *
 * @param x координата по оси X
 * @param y координата по оси Y
 * @param label подпись
 * @param date дата, соответствующая координте [y]
 */
internal data class LineAreaNode(
    val x: Float,
    val y: Float,
    val label: String?,
    val date: Date,
)