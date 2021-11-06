package otus.homework.customview.views

import android.os.Parcel
import android.os.Parcelable

data class LineData(val name: String, val amount: Int, val time: Long) : Parcelable {
    var x = 0f
    var y = 0f

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
       return 0
    }

    companion object CREATOR : Parcelable.Creator<LineData> {
        override fun createFromParcel(parcel: Parcel) = LineData(parcel)
        override fun newArray(size: Int): Array<LineData?> = arrayOfNulls(size)
    }
}