package otus.homework.customview.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Store(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
    var percentAmount: Float = 0f,
    var startAngle: Float = 0f,
    var sweepAngle: Float = 0f,
): Parcelable