package otus.homework.customview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Segment(val name: String, val color: Int, val value: Float, val time: Int) : Parcelable