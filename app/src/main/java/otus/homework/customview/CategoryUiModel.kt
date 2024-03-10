package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryUiModel(
    val category: String,
    val valueForDate: List<Pair<Int, Int>>
):Parcelable
