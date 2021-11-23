package otus.homework.customview.graph

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Point(
    val x: Float,
    val y: Float
) : Parcelable