package otus.homework.customview.pie_chart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CategoryArc(
    val categoryName: String,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Int
) : Parcelable