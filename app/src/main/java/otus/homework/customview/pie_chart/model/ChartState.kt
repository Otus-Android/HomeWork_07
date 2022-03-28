package otus.homework.customview.pie_chart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ChartState(
    val listCategory: List<CategoryArc>,
    var selectedCategory: CategoryArc?
) : Parcelable