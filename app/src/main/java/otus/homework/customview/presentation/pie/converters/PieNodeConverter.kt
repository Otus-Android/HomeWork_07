package otus.homework.customview.presentation.pie.converters

import android.graphics.Color
import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.pie.chart.PieNode
import kotlin.random.Random

/**
 * Конвертер данных узла кругового графика
 */
class PieNodeConverter {

    /**
     * Конвертировать [Category] в [PieNode]
     */
    fun convert(category: Category) = PieNode(
        value = category.amount.toFloat(),
        label = category.name,
        color = nextColor()
    )

    private fun nextColor() = Color.argb(
        MAX_COLOR,
        Random.nextInt(MAX_COLOR),
        Random.nextInt(MAX_COLOR),
        Random.nextInt(MAX_COLOR)
    )

    private companion object {
        const val MAX_COLOR = 255
    }
}