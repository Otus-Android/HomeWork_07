package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState

class CategoryState(superState: Parcelable?) : BaseSavedState(superState) {

    var categoryList: List<Category> = listOf()

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(categoryList)
    }
}
