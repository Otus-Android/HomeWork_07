package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import otus.homework.customview.entities.Spending

@Parcelize
data class TimelineFragmentSaveState(
    val spendingList: MutableList<Spending>
) : Parcelable