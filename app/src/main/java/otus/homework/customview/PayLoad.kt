package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class PayLoad(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
    var date: Date
): Parcelable