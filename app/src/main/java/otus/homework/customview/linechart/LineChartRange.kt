package otus.homework.customview.linechart

import android.graphics.PointF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LineChartRange(
    val points: List<PointF>,
    val color: Int
) : Parcelable
