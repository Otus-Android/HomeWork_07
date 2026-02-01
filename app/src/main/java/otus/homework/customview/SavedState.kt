package otus.homework.customview

import android.os.Parcelable
import android.view.View

class SavedState : View.BaseSavedState {

    var selectedCategoryIndex: Int = -1
    var selectedCategoryName: String? = null

    constructor(superState: Parcelable?) : super(superState)

    constructor(parcel: android.os.Parcel) : super(parcel) {
        selectedCategoryIndex = parcel.readInt()
        selectedCategoryName = parcel.readString()
    }

    override fun writeToParcel(out: android.os.Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(selectedCategoryIndex)
        out.writeString(selectedCategoryName)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: android.os.Parcel) = SavedState(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
        }
    }

}