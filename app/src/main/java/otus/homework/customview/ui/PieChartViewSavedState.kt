package otus.homework.customview.ui

import android.os.Parcelable
import android.view.View
import otus.homework.customview.entities.Arc

class PieChartViewSavedState(
    val arcs: MutableList<Arc>,
    private val state: Parcelable?
) : View.BaseSavedState(state)