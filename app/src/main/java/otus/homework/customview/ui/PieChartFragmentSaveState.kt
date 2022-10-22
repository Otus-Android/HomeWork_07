package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import otus.homework.customview.entities.Category

@Parcelize
data class PieChartFragmentSaveState(
    val categories: MutableList<Category>
) : Parcelable