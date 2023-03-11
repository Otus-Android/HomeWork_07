package otus.homework.customview.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartModel(
    val category: String,
    val percent: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val endAngle: Float,
    val colorPair: Pair<Int, Int>,
    val isSelected: Boolean = false
) : Parcelable {

    fun getBisectorAngle() = startAngle + sweepAngle / 2
}