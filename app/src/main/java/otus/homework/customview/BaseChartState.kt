package otus.homework.customview

import android.os.Parcelable
import android.view.View

class BaseChartState(
    private val superSavedState: Parcelable?,
    val dataList: List<PayLoadModel>): View.BaseSavedState(superSavedState), Parcelable