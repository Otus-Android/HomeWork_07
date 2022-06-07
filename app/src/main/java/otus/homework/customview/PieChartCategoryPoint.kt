package otus.homework.customview

import androidx.annotation.ColorInt

data class PieChartCategoryPoint(
    val category: String,
    val polygon: Polygon,
    @ColorInt
    val color: Int,
    val start: Float,
    val sweep: Float
)