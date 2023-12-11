package otus.homework.customview.presentation.pie.chart

import androidx.annotation.ColorInt

/**
 * Данные узла круговой диаграмы
 *
 * @param value значение
 * @param label подпись
 * @param color цвет узла
 */
data class PieNode(
    val value: Float,
    val label: String? = null,
    @ColorInt val color: Int,
    val payload: Any?
)
