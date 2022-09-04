package otus.homework.customview

import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PieSlice(
    val category: String,
    var value: Double,
    var startAngle: Float,
    var rotationAngle: Float,
    var indicatorCircleLocation: PointF,
    val paint: @RawValue Paint,
    var state: PieState = PieState.MINIMIZED
): Parcelable
