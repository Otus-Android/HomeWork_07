package otus.homework.customview.piechart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartSection(
    val pieChartItems: List<PieChartItem>,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Int
) : Parcelable