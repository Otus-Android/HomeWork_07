package otus.homework.customview.ui.pie_chart

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartItem(
    val id: Int,
    val label: String,
    val amount: Int,
    @ColorInt val segmentColor: Int
) : Parcelable
