package otus.homework.customview.views

import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable

class PieSlice(val name: String, val angle: Float) : Parcelable {
    val path = Path()

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeFloat(angle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PieSlice> {
        override fun createFromParcel(parcel: Parcel) = PieSlice(parcel)
        override fun newArray(size: Int): Array<PieSlice?> = arrayOfNulls(size)
    }
}