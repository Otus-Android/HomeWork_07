package otus.homework.customview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategorySlice(
    val name: String,
    val color: Int,
    val amount: Int,
    val percentage: Float,
    val startAngle: Float,
    val sweepAngle: Float,
) : Parcelable
