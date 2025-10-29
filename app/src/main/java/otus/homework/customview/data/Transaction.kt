package otus.homework.customview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
) : Parcelable
