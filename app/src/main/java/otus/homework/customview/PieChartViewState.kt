package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import otus.homework.customview.model.Store

@Parcelize
class PieChartViewState(val superSavedState: Parcelable?, val stores: ArrayList<Store>): Parcelable