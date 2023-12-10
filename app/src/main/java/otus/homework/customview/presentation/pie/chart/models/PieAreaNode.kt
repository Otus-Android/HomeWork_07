package otus.homework.customview.presentation.pie.chart.models

import androidx.annotation.ColorInt

/**
 * Внутренняя модель узла кругового графика
 *
 * @param startAngle угол начала
 * @param sweepAngle угол сектора
 * @param label подпись
 * @param color цвет
 */
internal data class PieAreaNode(
    val startAngle: Float,
    val sweepAngle: Float,
    val label: String?,
    @ColorInt val color: Int
)
