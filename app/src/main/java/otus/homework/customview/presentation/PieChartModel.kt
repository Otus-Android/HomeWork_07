package otus.homework.customview.presentation

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize


@Parcelize
data class PieChartUiInfo(
    val spendingInfo: List<PieChartSpendingItem>
): Parcelable

@Parcelize
data class PieChartSpendingItem(
    @field:ColorInt val color: Int,
    val amount: Int,
    val total: Int,
    val category: String
): Parcelable