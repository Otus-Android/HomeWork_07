package otus.homework.customview.presentation.pie.chart

import androidx.annotation.ColorInt

data class PieNode(
    val value: Float,
    val label: String? = null,
    @ColorInt val color: Int
)
