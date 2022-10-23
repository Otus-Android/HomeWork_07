package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import otus.homework.customview.entities.Category

@Parcelize
data class PieChartFragmentSaveState(
    val categories: MutableList<Category>
) : Parcelable