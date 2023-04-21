package otus.homework.customview.piechart

import android.graphics.PointF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieSlice(
    val name: String,
    var value: Float,
    var startAngle: Float,
    var sweepAngle: Float,
    val paintIndex: Int
): Parcelable