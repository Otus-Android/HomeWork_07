package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ListData  (
    var data:Map<String, Int>
): Parcelable
