package otus.homework.customview.linechart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LineChartItem(
    val category: String,
    val amount: Int,
    val time: Long
) : Parcelable