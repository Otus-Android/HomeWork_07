package otus.homework.customview.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: Int = UNDEFINED_ID,
    val name: String,
    var total: Int = 0,
    var color: Int = 0
) : Parcelable {

    companion object {
        const val UNDEFINED_ID = -1
    }
}
