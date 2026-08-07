package otus.homework.customview.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DailySpendItem(
    val category: String = "",
    val dayNum: Int = 0,
    val amount: Int = 0
) : Parcelable
