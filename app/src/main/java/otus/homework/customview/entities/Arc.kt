package otus.homework.customview.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Arc(
    val startAngle: Float,
    var sweepAngle: Float,
    val category: Category
) : Parcelable
