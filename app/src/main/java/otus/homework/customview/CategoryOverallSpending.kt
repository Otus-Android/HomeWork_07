package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryOverallSpending(
    val category: Category,
    val amount: Int
) : Parcelable
