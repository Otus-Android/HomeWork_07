package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayLoad(
    val id: Long,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
): Parcelable
