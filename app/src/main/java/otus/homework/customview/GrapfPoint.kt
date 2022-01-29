package otus.homework.customview

import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable

data class GrapfPoint(
    val name: String,
    val valuePoint: Float,
    val date: Float,
    var prosent: Float,
    var x: Float,
    var y: Float
)