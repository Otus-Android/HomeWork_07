package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable

class LineData(val amount: Int, val category: String?, val time: Long) : Parcelable {
    var x = 0f
    var y = 0f

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong()
    ) {
        x = parcel.readFloat()
        y = parcel.readFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(category)
        parcel.writeLong(time)
        parcel.writeFloat(x)
        parcel.writeFloat(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LineData> {
        override fun createFromParcel(parcel: Parcel): LineData {
            return LineData(parcel)
        }

        override fun newArray(size: Int): Array<LineData?> {
            return arrayOfNulls(size)
        }
    }
}