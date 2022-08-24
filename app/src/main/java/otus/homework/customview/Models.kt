package otus.homework.customview

import androidx.annotation.ColorInt

data class Expenditure(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)

data class ChartSegment(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val startAngle: Float,
    val endAngle: Float,
    val percentageOfMaximum: Float,
    @ColorInt val color: Int
) {

    val segmentAngle = endAngle - startAngle
}