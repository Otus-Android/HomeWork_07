package otus.homework.customview.piechart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartItem(
    val name: String,
    val category: String,
    val amount: Int
) : Parcelable