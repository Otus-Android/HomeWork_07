package otus.homework.customview.ui

import android.os.Parcelable
import android.view.View
import androidx.annotation.ColorInt

class TimelineViewSavedState(
//    val spendingList: MutableList<Spending>,
    @ColorInt val categoryColor: Int,
    private val state: Parcelable?
) : View.BaseSavedState(state)