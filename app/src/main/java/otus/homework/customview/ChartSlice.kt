package otus.homework.customview

import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable

data class ChartSlice(
    val name: String,
    val angle: Float,
    val angle2: Float,
    val angle3: Float,
    val path: Path = Path()
)