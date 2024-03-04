package otus.homework.customview.view

import android.os.Parcel

import android.os.Parcelable

import android.os.Parcelable.Creator
import android.view.View


class CustomViewSavedState : View.BaseSavedState {
    var lastIndex = 0

    constructor(superState: Parcelable?) : super(superState)
    private constructor(source: Parcel) : super(source) {
        lastIndex = source.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(lastIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<CustomViewSavedState> {
        override fun createFromParcel(parcel: Parcel): CustomViewSavedState {
            return CustomViewSavedState(parcel)
        }

        override fun newArray(size: Int): Array<CustomViewSavedState?> {
            return arrayOfNulls(size)
        }
    }
}